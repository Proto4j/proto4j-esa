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

package io.github.proto4j.crypto; //@date 23.01.2023

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * Basic implementation of a {@code java.security} cipher engine. Is it using
 * the default cipher instance of {@code AES/CBC/PKCS5Padding} for decryption
 * and encryption.
 * <p>
 * The following workflow illustrates how this cipher engine decrypts an input
 * JAR-file with a {@link SecretKey}:
 * <pre>
 * +-----------------------+
 * | encrypted JAR: byte[] |
 * +----------+------------+
 *            |
 *            |
 *            | Base64.decode()
 *            |
 *            |
 * +----------v------------+--------------+
 * | encrypted JAR: byte[] | iv: byte[16] |
 * +----------+------------+--------------+
 *            |
 *            | AESCipher.init(secretKey, iv)
 *            |
 *            | AESCipher.doFinal()
 *            |
 * +----------v------------+
 * | decrypted JAR: byte[] |
 * +-----------------------+
 * </pre>
 *
 * @see ICipher
 */
final class DefaultAESCipher implements ICipher {

    /**
     * A simple Object that synchronizes all incoming callers
     * when the AES-{@link Cipher} should be initialized.
     */
    private final Object aesInitLock = new Object();

    /**
     * A simple Object that synchronizes all incoming callers
     * when the AES-{@link Cipher} is created
     */
    private final Object aesWriteLock = new Object();

    /**
     * AES/CBC/PKCS5Padding Cipher which is used to de- and
     * encrypt incoming data.
     */
    private Cipher aes;

    /**
     * The current cipher state.
     */
    private volatile int mode;

    /**
     * The {@link SecretKeySpec} for the AES algorithm.
     */
    private SecretKey key;

    /**
     * Returns the initialization vector (IV) in a new buffer.
     *
     * <p> This is useful in the context of password-based encryption or
     * decryption, where the IV is derived from a user-provided passphrase.
     *
     * @return the initialization vector in a new buffer, or null if the
     *         underlying algorithm does not use an IV, or if the IV has not yet
     *         been set.
     */
    public byte[] getIV() {
        return aes.getIV();
    }

    @Override
    public void init(int mode, Key key, SecureRandom random) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Key is not a secret key");
        }

        this.mode = mode;
        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE) {
            throw new UnsupportedOperationException("Cipher mode not supported");
        }

        try {
            synchronized (aesWriteLock) {
                if (this.aes == null) {
                    this.aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                }
            }
            this.key = (SecretKey) key;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void init(int mode, Key key) throws InvalidKeyException {
        init(mode, key, new SecureRandom());
    }

    @Override
    public void init(int mode, Key key, AlgorithmParameterSpec spec) throws InvalidKeyException {
        init(mode, key);
    }

    /**
     * Decrypts the given bytes by applying the stored {@link SecretKey}.
     *
     * @param bytes the encrypted content
     * @return a byte array representing the decrypted content.
     * @throws IllegalBlockSizeException if the decoded content is smaller than 16 bytes
     */
    private synchronized byte[] doDecrypt(byte[] bytes) throws IllegalBlockSizeException {
        byte[] result;
        byte[] iv;
        byte[] content;
        byte[] secretKey = key.getEncoded();

        if (secretKey.length != 16) {
            throw new IllegalBlockSizeException("Key.length != 16");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(bytes);
            if (decoded.length <= 16) {
                throw new BadPaddingException("Content.length <= 16");
            }

            ByteBuffer buffer = ByteBuffer.allocate(decoded.length);
            buffer.put(decoded).flip();

            iv = new byte[16];
            content = new byte[decoded.length - 16];
            buffer.get(iv);
            buffer.get(content);

            synchronized (aesInitLock) {
                aes.init(mode, key, new IvParameterSpec(iv));
                result = aes.doFinal(content);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypts the given bytes by applying the stored {@link SecretKey}.
     *
     * @param bytes the plain content
     * @return a byte array representing the encrypted content.
     * @throws IllegalBlockSizeException if the key length is not 16 bytes
     */
    private synchronized byte[] doEncrypt(byte[] bytes) throws IllegalBlockSizeException {
        byte[] result;
        byte[] iv;
        byte[] secretKey = key.getEncoded();

        if (secretKey.length != 16) {
            throw new IllegalBlockSizeException("Key.length != 16");
        }

        try {
            synchronized (aesInitLock) {
                aes.init(mode, key, (SecureRandom) null);
                result = aes.doFinal(bytes);
                iv = getIV();
            }

            int len = result.length + iv.length;
            ByteBuffer buffer = ByteBuffer.allocate(len);
            buffer.put(iv);
            buffer.put(result).flip();

            byte[] finalResult = new byte[len];
            buffer.get(finalResult);
            return Base64.getEncoder().encode(finalResult);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] doFinal(byte[] input) throws IllegalBlockSizeException {
        return doFinal(input, 0, input.length);
    }

    public byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException {
        byte[] result = new byte[0];

        if (mode == Cipher.DECRYPT_MODE) {
            result = doDecrypt(input);
        } else if (mode == Cipher.ENCRYPT_MODE) {
            result = doEncrypt(input);
        }

        key = null;
        return result;
    }

}
