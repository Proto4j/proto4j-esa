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

package io.github.proto4j.crypto.provider; //@date 27.01.2023

import io.github.proto4j.crypto.key.DestroyableSecretKey;

/**
 * Native key providers should use a library to in order to get the
 * used decryption and encryption key.
 * <p>
 * A basic implementation of this class could be the following:
 * <pre>
 *     class FooLibKeyProvider extends NativeKeyProvider {
 *         public FooLibKeyProvider() {
 *             super("foo_native");
 *         }
 *
 *         public native byte[] getKey0();
 *
 *         &#064;Override
 *         public Key getSecretKey() {
 *             return new {@link DestroyableSecretKey}(getKey0());
 *         }
 *     }
 * </pre>
 *
 * @see KeyProvider
 */
public abstract class NativeKeyProvider extends KeyProvider {

    /**
     * The library name
     */
    private final String libName;

    /**
     * Creates a new {@code NativeKeyProvider} with the given native library
     * name and tries load it.
     *
     * @param libName the library to load
     */
    public NativeKeyProvider(String libName) {
        this.libName = libName;
        if (!loadLibrary()) {
            throw new IllegalStateException("Could not load library with name: " + libName);
        }
    }

    /**
     * Tries to load the shared library
     *
     * @return {@code true} on success
     */
    public boolean loadLibrary() {
        try {
            System.loadLibrary(libName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the configured library name.
     *
     * @return the library name
     */
    public String getLibraryName() {
        return libName;
    }
}
