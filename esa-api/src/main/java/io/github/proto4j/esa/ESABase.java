/*
 * Copyright 2023 Proto4j
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.proto4j.esa; //@date 27.01.2023

import io.github.proto4j.crypto.ICipher;
import io.github.proto4j.crypto.key.DestroyableSecretKey;
import io.github.proto4j.crypto.key.SecureSecretKey;
import io.github.proto4j.crypto.provider.KeyProvider;
import io.github.proto4j.esa.executor.InvocationException;
import io.github.proto4j.esa.executor.SharedInvocationException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.zip.ZipInputStream;

/*
 * Basic implementation of an ESA. Class caching should be implemented by
 * subclasses. There is only one method that should be used to access the
 * stored secret key (#prepareCipher).
 */
public abstract class ESABase extends ESA {

    protected ICipher cipher;
    protected JarConfiguration configuration;
    protected ClassLoader classLoader;

    private volatile boolean loaded = false;

    public ESABase(KeyProvider provider, ICipher cipher, JarConfiguration configuration,
                   ClassLoader classLoader) {
        super(provider);
        this.cipher = cipher;
        this.configuration = configuration;
        this.classLoader = classLoader;
    }

    protected ESABase(KeyProvider provider) {
        super(provider);
    }

    protected abstract Class<?> getClass(String className);

    @Override
    public Method getSharedMethod(String cls, String mth, Class<?>... argTypes)
            throws NoSuchSharedMethodException, NoSuchSharedClassException {
        Class<?> cachedClass = getClass(cls);
        String decName = null;

        if (cachedClass == null) try {
            prepareCipher(Cipher.DECRYPT_MODE);
            String name = new String(cipher.doFinal(cls.getBytes()));
            cachedClass = getClass(name);
        } catch (Exception e) {
            throw new NoSuchSharedClassException("Could not locate class", e);
        }

        if (cachedClass == null) {
            throw new NoSuchSharedClassException("Could not locate class: " + cls);
        }

        Method target = null;
        try {
            target = cachedClass.getDeclaredMethod(mth, argTypes);
        } catch (NoSuchMethodException e) {
            try {
                prepareCipher(Cipher.DECRYPT_MODE);
                decName = new String(cipher.doFinal(mth.getBytes()));

                target = cachedClass.getDeclaredMethod(decName, argTypes);
            } catch (NoSuchMethodException e2) {
                throw new NoSuchSharedMethodException("Could not locate method: " + decName);
            } catch (Exception ignored) {
            }
        }

        if (target == null) {
            throw new NoSuchSharedMethodException("Could not locate shared method!");
        }

        return target;
    }

    @Override
    public Field getSharedField(String cls, String name, boolean inherited) throws NoSuchSharedFieldException {
        Class<?> cachedClass = getClass(cls);

        if (cachedClass == null) try {
            prepareCipher(Cipher.DECRYPT_MODE);

            String clsName = new String(cipher.doFinal(cls.getBytes()));
            cachedClass = getClass(clsName);
        } catch (Exception e) {
            throw new NoSuchSharedClassException("Could not locate shared class", e);
        }

        if (cachedClass == null) {
            throw new NoSuchSharedClassException("Could not locate class: " + cls);
        }

        String decName = null;
        Field target = null;
        try {
            target = cachedClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                prepareCipher(Cipher.DECRYPT_MODE);
                decName = new String(cipher.doFinal(name.getBytes()));
                target = cachedClass.getDeclaredField(decName);
            } catch (Exception e2) {
                if (!inherited) {
                    throw new NoSuchSharedFieldException("Could not locate Field", e2);
                }
            }
        }

        if (target == null) {
            Class<?> base = cachedClass;
            while (((base = base.getSuperclass()) != Object.class) && target == null) {
                try {
                    target = base.getDeclaredField(name);
                } catch (Exception e) {
                    try {
                        if (decName != null) {
                            target = base.getDeclaredField(decName);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (target == null) {
            throw new NoSuchSharedFieldException("Could not locate shared field!");
        }

        return target;
    }

    protected final byte[] getZipEntryContent(ZipInputStream zis) throws IOException {
        return getZipEntryContent(zis, 2048);
    }

    protected final byte[] getZipEntryContent(ZipInputStream zis, int bufferSize) throws IOException {
        byte[] content;
        byte[] buffer = new byte[bufferSize];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }

            content = bos.toByteArray();
        }
        return content;
    }

    @Override
    public Object getInstance(String cls, Object... argv) throws SharedException {
        Class<?> clsInstance = null;
        try {
            clsInstance = getClass(cls);
        } catch (Exception e) {
            try {
                prepareCipher(Cipher.DECRYPT_MODE);
                String name = new String(cipher.doFinal(cls.getBytes()));
                clsInstance = getClass(name);
            } catch (Exception ex) {
                throw new NoSuchSharedClassException("Class not found", ex);
            }
        }
        try {
            if (argv == null ||argv.length == 0) {
                return clsInstance.getDeclaredConstructor().newInstance();
            }

            Class<?>[] classes = new Class[argv.length];
            for (int i = 0; i < argv.length; i++) {
                classes[i] = argv[i] != null ? argv[i].getClass() : null;
            }

            for (Constructor<?> constructor : clsInstance.getDeclaredConstructors()) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length == classes.length) {
                    if (isEqual(classes, types)) {
                        return constructor.newInstance(argv);
                    }
                }
            }
            return clsInstance.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException e) {
            throw new SharedInvocationException(e.getTargetException());
        } catch (Exception e) {
            throw new InvocationException(e);
        }
    }

    private static boolean isEqual(Class<?>[] classes, Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            if (classes[i] != null && (!types[i].isAssignableFrom(classes[i]))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ICipher getAESCipher() {
        return cipher;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public JarConfiguration getConfiguration() {
        return configuration;
    }

    protected synchronized final void prepareCipher(int mode) {
        try {
            KeyProvider keyProvider = provider.get();
            Key secretKey = keyProvider.getSecretKey();

            if (secretKey instanceof SecureSecretKey) {
                SecureSecretKey key = (SecureSecretKey) secretKey;
                try (SecureSecretKey.Accessor accessor = key.getAccessor()) {
                    SecretKey dk = new DestroyableSecretKey(accessor.getEncoded(), secretKey.getAlgorithm());
                    cipher.init(mode, dk);
                    dk.destroy();
                }
            } else {
                cipher.init(mode, secretKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized final boolean isLoaded() {
        return loaded;
    }

    protected synchronized final void setLoaded() {
        this.loaded = true;
    }
}
