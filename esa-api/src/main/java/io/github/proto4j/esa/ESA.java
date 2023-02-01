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

import io.github.proto4j.crypto.ICipher;
import io.github.proto4j.crypto.provider.KeyProvider;
import io.github.proto4j.esa.annotation.Encrypt;
import io.github.proto4j.esa.annotation.Output;
import io.github.proto4j.esa.api.asm.StaticBlockWriter;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * When speaking of an embedded shared archive (ESA), an encrypted JAR file that
 * is stored inside a small Java class file is referenced.
 * <p>
 * Although, an ESA does not contain any other resources than the standard manifest
 * file by default, it is possible that a generated DEX-file is stored inside. The
 * DEX-file is required when working on Android projects.
 * <p>
 * Generated and encrypted JAR files will be stored in Java one class that implements
 * the ESAFile interface.
 *
 * @see ESAFile
 */
public abstract class ESA {

    /**
     * A final {@link Type} instance used when manipulating the bytecode of
     * compiled classes.
     *
     * @see StaticBlockWriter
     */
    public static final Type TYPE = Type.getType(ESA.class);

    /**
     * Defines the output field that stores the ESA file.
     */
    public static final String ENCODED = "encodedJar";

    /**
     * Defines the output field that stored the ESA's name.
     */
    public static final String NAME = "filename";

    /**
     * The key provider to use.
     */
    protected final ThreadLocal<KeyProvider> provider;

    /**
     * Creates a new <code>ESA</code> instance with the given key provider.
     *
     * @param provider the provider to use
     */
    protected ESA(KeyProvider provider) {
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
     *         static final String bar = {@link ESA}.wrap();
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
     *             bar = {@link ESA}.wrap();
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

    /**
     * Resolves a {@link Method} by its declaring class, name and argument
     * types. This function can be called with encrypted and unencrypted method
     * and class names.
     *
     * @param cls the declaring class
     * @param mth the method's name
     * @param argTypes the method's argument type classes
     * @return the resolved method (always non-null)
     * @throws NoSuchSharedMethodException if the method could not be located in
     *                                     the given class
     * @throws NoSuchSharedClassException  if the declaring class could not be
     *                                     resolved
     */
    public abstract Method getSharedMethod(final String cls, final String mth, Class<?>... argTypes)
            throws NoSuchSharedMethodException, NoSuchSharedClassException;

    /**
     * Resolves a {@link Field} by its declaring class and name. This function
     * can be called with encrypted and unencrypted field and class names. In
     * addition, inherited fields will be also included if the parameter
     * <code>inherited</code> is set to true.
     *
     * @param cls the declaring class
     * @param name the field's name
     * @param inherited whether the field is inherited from a base class
     * @return the resolved method (always non-null)
     * @throws NoSuchSharedFieldException if the field could not be located in
     *                                    the given class or base classes
     * @throws NoSuchSharedClassException if the declaring class could not be
     *                                    resolved
     */
    public abstract Field getSharedField(final String cls, final String name, final boolean inherited)
            throws NoSuchSharedFieldException, NoSuchSharedClassException;

    /**
     * Creates a new instance of the given class with the provided arguments.
     *
     * @param cls the class type
     * @param argv the argument values
     * @return the new instance of the given type
     * @throws NoSuchSharedClassException if the class could not be resolved
     * @throws SharedException            if any error occurs while creating the
     *                                    new instance
     */
    public abstract Object getInstance(final String cls, Object... argv)
            throws NoSuchSharedClassException, SharedException;

    /**
     * Loads the given ESA file. This operation may fail if this object has
     * already loaded an ESA. Use {@link #isLoaded()} to check that.
     *
     * @param file the file to load
     * @throws GeneralSecurityException if an error occurs while decrypting
     * @throws IOException              if an error occurs while reading the JAR file
     */
    //autoload on creation
    public abstract void load(final ESAFile file)
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

    /**
     * Returns the configuration for this {@code ESA}.
     *
     * @return the configuration for this {@code ESA}.
     */
    public abstract JarConfiguration getConfiguration();

    /**
     * Returns whether an ESA file was imported recently.
     *
     * @return whether this object has already imported an ESA file.
     * @see #load(ESAFile)
     */
    public abstract boolean isLoaded();

    /**
     * An abstract builder class to simplify the creation of {@link ESA}
     * instances.
     * <p>
     * The following table contains information about which variables can be
     * configured:
     * <table>
     *     <caption>Available configuration</caption>
     *     <tr>
     *         <th>Name</th>
     *         <th>Description</th>
     *     </tr>
     *     <tr>
     *         <td>{@link JarConfiguration}</td>
     *         <td>Used as a tagging interface to enable dynamic object
     *         configuration. By default, there is no configuration inherited
     *         from the base class. When using an <code>AndroidJarConfiguration</code>
     *         the current <code>Context</code> has to be specified as a single
     *         configuration parameter.</td>
     *     </tr>
     *     <tr>
     *          <td>{@link KeyProvider}</td>
     *          <td>An object storing and providing the decryption key. Usually,
     *          this class deserves a custom implementation, because using the
     *          <code>PlainTextKeyProvider</code> is not that secure. There is
     *          also a possibility to create native key providers - keys are
     *          returned by invoking native methods. </td>
     *     </tr>
     *     <tr>
     *         <td>{@link ICipher}</td>
     *         <td>The cipher implementation used to decrypt the ESA file. By
     *         default, {@link ICipher#newDefaultInstance()} is called to retrieve
     *         an instance.</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Output}</td>
     *         <td>This options can have multiple ways on how to configure it:
     *         <br>
     *         <ul>
     *             <li>You can write <code>new YourOutputClass()</code> to add
     *             an instance of the marked output class directly, or</li>
     *             <li>You can pass the class object of the annotated output class,
     *             or</li>
     *             <li>You provide a <code>Supplier</code> that returns an instance
     *             of the <code>ESAFile</code> class</li>
     *        </ul>
     *        </td>
     *     </tr>
     * </table>
     *
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> {

        public abstract T configure(JarConfiguration configuration);

        public abstract T setProvider(KeyProvider provider);

        public abstract T setOutputClass(Class<?> cls);

        public abstract T setOutputObject(Object outputObject);

        @Deprecated
        public abstract T setClassLoader(ClassLoader classLoader);

        public abstract T setCipher(ICipher cipher);

        public abstract ESA finish();

        public T configure(Supplier<? extends JarConfiguration> provider) {
            Objects.requireNonNull(provider);

            return configure(provider.get());
        }

        public T setProvider(Supplier<? extends KeyProvider> supplier) {
            Objects.requireNonNull(supplier);

            return setProvider(supplier.get());
        }

        public T setOutputObject(Supplier<Object> provider) {
            Objects.requireNonNull(provider);

            return setOutputObject(provider.get());
        }

        @Deprecated
        public T setClassLoader(Supplier<? extends ClassLoader> provider) {
            Objects.requireNonNull(provider);
            return setClassLoader(provider.get());
        }

        public T setCipher(Supplier<? extends ICipher> provider) {
            Objects.requireNonNull(provider);
            return setCipher(provider);
        }

    }
}
