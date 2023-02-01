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

package io.github.proto4j.crypto.key; //@date 28.01.2023

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

/**
 * Reimplementation of the {@link javax.crypto.spec.SecretKeySpec} by Java that
 * destroys the stored secret key upon calling {@link #destroy()}.
 *
 * @see javax.crypto.spec.SecretKeySpec
 */
public class DestroyableSecretKey implements SecretKey {

    private final byte[] key;
    private final String algorithm;

    public DestroyableSecretKey(SecureSecretKey.Accessor accessor, String algorithm) {
        this(accessor.getEncoded(), algorithm);
    }

    public DestroyableSecretKey(byte[] key, String algorithm) {
        if (key != null && algorithm != null) {
            if (key.length == 0) {
                throw new IllegalArgumentException("Empty key");
            } else {
                this.key       = (byte[]) key.clone();
                this.algorithm = algorithm;
            }
        } else {
            throw new IllegalArgumentException("Missing argument");
        }
    }

    public DestroyableSecretKey(byte[] key, int offset, int len, String algorithm) {
        if (key != null && algorithm != null) {
            if (key.length == 0) {
                throw new IllegalArgumentException("Empty key");
            } else if (key.length - offset < len) {
                throw new IllegalArgumentException("Invalid offset/length combination");
            } else if (len < 0) {
                throw new ArrayIndexOutOfBoundsException("len is negative");
            } else {
                this.key = new byte[len];
                System.arraycopy(key, offset, this.key, 0, len);
                this.algorithm = algorithm;
            }
        } else {
            throw new IllegalArgumentException("Missing argument");
        }
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        return (byte[]) this.key.clone();
    }

    public int hashCode() {
        int retval = 0;

        for (int i = 1; i < this.key.length; ++i) {
            retval += this.key[i] * i;
        }

        return this.algorithm.equalsIgnoreCase(
                "TripleDES") ? retval ^ "desede".hashCode() : retval ^ this.algorithm.toLowerCase(
                Locale.ENGLISH).hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SecretKey)) {
            return false;
        } else {
            String thatAlg = ((SecretKey) obj).getAlgorithm();
            if (thatAlg.equalsIgnoreCase(this.algorithm) || thatAlg.equalsIgnoreCase(
                    "DESede") && this.algorithm.equalsIgnoreCase("TripleDES") || thatAlg.equalsIgnoreCase(
                    "TripleDES") && this.algorithm.equalsIgnoreCase("DESede")) {
                byte[] thatKey = ((SecretKey) obj).getEncoded();
                return MessageDigest.isEqual(this.key, thatKey);
            } else {
                return false;
            }
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        if (isDestroyed()) {
            return;
        }

        Arrays.fill(key, (byte)0);
    }
}
