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
import io.github.proto4j.crypto.provider.KeyProvider;
import io.github.proto4j.esa.api.ByteCodeClassLoader;
import io.github.proto4j.esa.api.TypeClassLoader;
import org.objectweb.asm.Type;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

final class DefaultESA extends ESABase {

    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    DefaultESA(KeyProvider provider, ICipher cipher, JarConfiguration configuration) {
        super(provider, cipher, configuration, new TypeClassLoader());
    }

    @Override
    protected Class<?> getClass(String className) {
        return loadedClasses.getOrDefault(className, null);
    }

    @Override
    public ByteCodeClassLoader<Type> getClassLoader() {
        return (TypeClassLoader) super.getClassLoader();
    }

    @Override
    public void load(ESAFile aesContent) throws GeneralSecurityException, IOException {
        if (isLoaded()) return;

        prepareCipher(Cipher.DECRYPT_MODE);
        byte[] file = cipher.doFinal(aesContent.getEncoded().getBytes());
        if (file == null) return;

        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file))) {

            JarEntry entry = null;
            while ((entry = jis.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (!name.endsWith(".class")) continue;

                name = name.replaceAll("/", ".");
                name = name.substring(0, name.length() - ".class".length());

                String descriptor = 'L' + name.replaceAll("\\.", "/") + ';';

                byte[] content = getZipEntryContent(jis);

                getClassLoader().put(Type.getType(descriptor), content);
                Class<?> cls = getClassLoader().loadClass(name);

                if (cls != null) {
                    loadedClasses.put(name, cls);
                }
            }
            setLoaded();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
