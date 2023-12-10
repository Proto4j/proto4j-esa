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

package io.github.proto4j.crypto.key;//@date 26.01.2023

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

/**
 * A secured secret key. This key is designed to return a copy of its encoded
 * key and remove it afterwards.
 *
 * @see Accessor
 */
public interface SecureSecretKey extends SecretKey {

    /**
     * Throws an {@code UnsupportedOperationException}; use {@link #getAccessor()}
     * instead
     *
     * @throws UnsupportedOperationException This method is designed to be unsafe,
     *                                       therefore use {@link #getAccessor()} to
     *                                       access the underlying key.
     */
    @Override
    @Deprecated
    default byte[] getEncoded() {
        throw new UnsupportedOperationException("@Unsafe operation");
    }

    /**
     * Returns the key accessor for this {@code SecureSecretKey} object.
     *
     * @return the accessor the retrieve the key of this object
     */
    Accessor getAccessor();

    /**
     * Delegation interface for the {@link SecretKey}. To retrieve the raw key,
     * this class can be used as follows:
     * <pre>
     *     try ({@link SecureSecretKey.Accessor} accessor = key.getAccessor()) {
     *         // key will be removed when leaving the try-resource block
     *         byte[] key = accessor.getEncoded();
     *     }
     * </pre>
     * To prevent memory leakage, this class will wipe the contents of the
     * returned encoded key after the {@link #close()} method is called.
     *
     * @see SecureSecretKey
     */
    public interface Accessor extends AutoCloseable, Destroyable {

        /**
         * Returns the key in its primary encoding format.
         *
         * @return the raw key
         */
        byte[] getEncoded();

        /**
         * Returns an instance of {@code DirectSecretKey} that will be cleared
         * afterwards.
         *
         * @return a {@code DirectSecretKey} with direct access to the key
         * @see DirectSecretKey
         */
        SecretKey getDirectAccessKey();

        /**
         * Sensitive information associated with this Object is destroyed or
         * cleared.
         *
         * @throws DestroyFailedException if the contents could not be destroyed
         */
        @Override
        void destroy() throws DestroyFailedException;

        /**
         * Closes this resource by calling {@link #destroy()}.
         *
         * @throws DestroyFailedException if the key could not be destroyed
         */
        @Override // overwrite key
        public default void close() throws Exception {
            if (isDestroyed()) {
                return;
            }

            destroy();
        }
    }

}
