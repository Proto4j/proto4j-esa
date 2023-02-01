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

package io.github.proto4j.crypto;//@date 23.01.2023

import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * An interface to allow custom cipher implementations to decrypt and
 * encrypt ESA files.
 *
 * @see DefaultAESCipher
 */
public interface ICipher {

    /**
     * Creates a new {@link DefaultAESCipher} instance.
     *
     * @return the created cipher
     */
    public static ICipher newDefaultInstance() {
        return new DefaultAESCipher();
    }

    /**
     * Gets the current operating mode.
     *
     * @return the opmode
     */
    public int getMode();

    /**
     * Initializes this cipher with a key and a set of algorithm parameters.
     *
     * @param mode the operation mode of this cipher (this is one of
     *         the following: <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>
     * @param key the encryption key
     * @param spec the algorithm parameters
     * @throws InvalidKeyException if the given key is inappropriate for
     *                             initializing this cipher, or requires
     *                             algorithm parameters that cannot be
     *                             determined from the given key.
     */
    public void init(int mode, Key key, AlgorithmParameterSpec spec)
            throws InvalidKeyException;

    /**
     * Initializes this cipher with a key.
     *
     * @param mode the operation mode of this cipher (this is one of
     *         the following: <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>
     * @param key the encryption key
     * @throws InvalidKeyException if the given key is inappropriate for
     *                             initializing this cipher, or requires
     *                             algorithm parameters that cannot be
     *                             determined from the given key.
     */
    public void init(int mode, Key key) throws InvalidKeyException;

    /**
     * Initializes this cipher with a key and a source of randomness.
     *
     * @param mode the operation mode of this cipher (this is one of
     *         the following: <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>
     * @param key the encryption key
     * @param random the source of randomness
     * @throws InvalidKeyException if the given key is inappropriate for
     *                             initializing this cipher, or requires
     *                             algorithm parameters that cannot be
     *                             determined from the given key.
     */
    public void init(int mode, Key key, SecureRandom random)
            throws InvalidKeyException;

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted, depending
     * on how this cipher was initialized.
     *
     * @param input the input buffer
     * @return the new buffer with the result
     * @throws IllegalBlockSizeException if this cipher is a block cipher,
     *                                   no padding has been requested (only
     *                                   in encryption mode), and the total
     *                                   input length of the data processed
     *                                   by this cipher is not a multiple of
     *                                   block size; or if this encryption
     *                                   algorithm is unable to process the
     *                                   input data provided.
     */
    public byte[] doFinal(byte[] input) throws IllegalBlockSizeException;

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted, depending
     * on how this cipher was initialized.
     * <p>
     * The first <code>length</code> bytes in the <code>input</code>
     * buffer, starting at <code>offset</code> inclusive. The result is stored
     * in a new buffer.
     *
     * @param input the input buffer
     * @param offset the offset in <code>input</code> where the input
     *         starts
     * @param length the input length
     * @return the new buffer with the result
     * @throws IllegalBlockSizeException if this cipher is a block cipher,
     *                                   no padding has been requested (only
     *                                   in encryption mode), and the total
     *                                   input length of the data processed
     *                                   by this cipher is not a multiple of
     *                                   block size; or if this encryption
     *                                   algorithm is unable to process the
     *                                   input data provided.
     */
    public byte[] doFinal(byte[] input, int offset, int length)
            throws IllegalBlockSizeException;
}
