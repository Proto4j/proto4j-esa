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

package io.github.proto4j.crypto.provider;

import io.github.proto4j.crypto.key.SecureSecretKey;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyException;

/**
 * Key provider classes are used to return a {@code SecretKey} instance. There
 * are multiple possibilities to implement this provider class. By default,
 * the following are provided:
 * <ul>
 *     <li><code>PlainTextKeyProvider</code>: a provider that stores an encoded
 *     version of the initial plain text key</li>
 *     <li><code>NativeKeyProvider</code>: classes that extend the native provider
 *     may be used to provide access to the secret key be calling a native method.
 *     </li>
 *     <li><code>BasicKeyProvider</code>: default provider for storing the
 *     initial secret key instance</li>
 * </ul>
 * For instance, Android projects might use a custom implementation of this
 * class whereby the {@link #getSecretKey()} method requests the secret key
 * on the internal <code>KeyStore</code> each time its called.
 */
public abstract class KeyProvider {

    /**
     * Returns the <code>Key</code> used for encryption and decryption. It
     * is preferred to use instances of <code>SecureSecretKey</code> due to
     * its structural design.
     *
     * @return the key used for encryption and decryption
     * @throws KeyException if an error occurs
     * @see SecureSecretKey
     */
    public abstract Key getSecretKey() throws KeyException;


}
