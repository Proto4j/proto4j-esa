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

package io.github.proto4j.crypto;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;

@Deprecated
public final class CryptoUtil {

    private CryptoUtil() {}

    public static String decodeKey(String content, byte key) throws IllegalArgumentException {
        return new String(decodeKey(content.getBytes(), key));
    }

    public static byte[] decodeKey(byte[] content, byte key) throws IllegalArgumentException {
        byte[] decoded = Base64.getDecoder().decode(content);
        if (decoded.length != 32) {
            throw new IllegalArgumentException("decoded.length != 32");
        }

        byte[] result = new byte[16];
        ByteBuffer.wrap(decoded, 4, 16).get(result);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (result[i] ^ key);
        }
        return result;
    }

    public static String encodeKey(String content, byte key, boolean force) throws IllegalArgumentException {
        return new String(encodeKey(content.getBytes(), key, force));
    }

    public static byte[] encodeKey(byte[] content, byte key, boolean force) throws IllegalArgumentException {
        if (content.length != 16) {
            if (!force) {
                throw new IllegalArgumentException("content.length != 16");
            }
            byte[] data = new byte[16];
            System.arraycopy(content, 0, data, 0, Math.min(16, content.length));
            content = data;
        }

        byte[] result = new byte[32];
        new Random().nextBytes(result);
        for (int i = 0; i < 16; i++) {
            result[i + 4] = (byte) (content[i] ^ key);
        }

        return Base64.getEncoder().encode(result);
    }

}
