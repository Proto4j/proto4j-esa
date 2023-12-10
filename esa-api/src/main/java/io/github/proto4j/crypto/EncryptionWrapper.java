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

package io.github.proto4j.crypto;//@date 24.01.2023

/**
 * Redefinition of the {@link java.util.function.Function} interface that
 * can throw any exception.
 *
 * @param <T> the input type
 * @param <R> the result type
 */
public interface EncryptionWrapper<T, R> {

    /**
     * Creates wrapper that returns the input value.
     *
     * @return the input value
     * @param <U> the input type
     */
    static <U> EncryptionWrapper<U, U> identity() {
        return x -> x;
    }

    /**
     * Encrypts the given input using a custom encryption algorithm or a
     * shared cipher instance.
     *
     * @param value the value to be encrypted
     * @return the encrypted value
     * @throws Exception if an error occurs during encryption
     */
    public abstract R encrypt(T value) throws Exception;
}
