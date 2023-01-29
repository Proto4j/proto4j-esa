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

package io.github.proto4j.esa; //@date 23.01.2023

import io.github.proto4j.crypto.provider.KeyProvider;
import io.github.proto4j.esa.annotation.Encrypt;
import io.github.proto4j.esa.api.asm.StaticBlockWriter;
import org.objectweb.asm.Type;
import io.github.proto4j.crypto.ICipher;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SharedJar {

    /**
     * A final {@link Type} instance used when manipulating the bytecode of
     * compiled classes.
     *
     * @see StaticBlockWriter
     */
    public static final Type TYPE = Type.getType(SharedJar.class);

    public static final String ENCODED = "encodedJar";
    public static final String NAME    = "filename";

    /**
     * The key provider to use.
     */
    protected final ThreadLocal<KeyProvider> provider;

    /**
     * Creates a new <code>SharedJar</code> instance with the given key provider.
     *
     * @param provider the provider to use
     */
    protected SharedJar(KeyProvider provider) {
        Objects.requireNonNull(provider);
        this.provider = new ThreadLocal<>();
        this.provider.set(provider);
    }

    /**
     * Using a simple null-return-value method to add an extra instruction to
     * the static initializer block (clinit). This method is a must when
     * annotating a field with {@code Encrypt}.
     * <p>
     * If you want to declare a new variable that should be seen in plain text
     * when decompiling, this method has to be used. For instance, imagine the
     * following situation:
     * <pre>
     *     class Foo {
     *         &#064;Encrypt("hello")
     *         static final String bar = {@link SharedJar}.wrap();
     *     }
     * </pre>
     * The code above would be translated into a static initializer block, where
     * {@code wrap} is going to be called.
     * <pre>
     *     class Foo {
     *         &#064;Encrypt("hello")
     *         static final String bar;
     *
     *         static {
     *             bar = {@link SharedJar}.wrap();
     *         }
     *     }
     * </pre>
     * Here, the {@code StaticBlockWriter} is used to apply the hardcoded
     * value given in {@code Encrypt} to the declared variable (the annotation
     * will be removed afterwards). Finally, the decompiled source code should
     * look like this:
     * <pre>
     *     class Foo {
     *         static final String foo = "Hello";
     *     }
     * </pre>
     * <p>
     * For more information about how the bytecode will be changed it provided
     * in {@link StaticBlockWriter}.
     *
     * @return {@code null}
     * @see StaticBlockWriter
     * @see Encrypt
     */
    //IMPORTANT
    public static String wrap() {
        return null;
    }

    public abstract Method getSharedMethod(final String cls, final String mth, Class<?>... argTypes)
            throws NoSuchSharedMethodException, NoSuchSharedClassException;

    public abstract Field getSharedField(final String cls, final String name, final boolean recursive)
            throws NoSuchSharedFieldException, NoSuchSharedClassException;

    public abstract Object getInstance(final String cls)
            throws NoSuchSharedClassException, SharedException;

    //autoload on creation
    public abstract void load(final ESAFile aesContent)
            throws GeneralSecurityException, IOException;

    /**
     * Returns the configured <code>AESCipher</code> instance. Note that you
     * have to provide the encryption/ decryption key for every action on this
     * cipher, because it will be removed afterwards.
     *
     * @return the used cipher instance
     */
    public abstract ICipher getAESCipher();

    /**
     * Returns the <code>ClassLoader</code> that will be used later on to load
     * each individual class. This can be a <code>DexClassLoader</code> on
     * Android devices as well as the normal system classloader.
     *
     * @return the current configured class loader
     */
    public abstract ClassLoader getClassLoader();

    public abstract JarConfiguration getConfiguration();

    public abstract boolean isLoaded();

    public static abstract class Builder<T extends Builder<T>> {

        public abstract T configure(JarConfiguration configuration);

        public T configure(Supplier<? extends JarConfiguration> provider) {
            Objects.requireNonNull(provider);

            return configure(provider.get());
        }

        public T setProvider(Supplier<? extends KeyProvider> supplier) {
            Objects.requireNonNull(supplier);

            return setProvider(supplier.get());
        }

        public abstract T setProvider(KeyProvider provider);

        public abstract T setOutputClass(Class<?> cls);

        public T setOutputObject(Supplier<Object> provider) {
            Objects.requireNonNull(provider);

            return setOutputObject(provider.get());
        }

        public abstract T setOutputObject(Object outputObject);

        @Deprecated
        public abstract T setClassLoader(ClassLoader classLoader);

        @Deprecated
        public T setClassLoader(Supplier<? extends ClassLoader> provider) {
            Objects.requireNonNull(provider);
            return setClassLoader(provider.get());
        }

        public abstract T setCipher(ICipher cipher);

        public T setCipher(Supplier<? extends ICipher> provider) {
            Objects.requireNonNull(provider);
            return setCipher(provider);
        }

        public abstract SharedJar finish();

    }
}
