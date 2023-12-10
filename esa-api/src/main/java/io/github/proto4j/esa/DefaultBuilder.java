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

package io.github.proto4j.esa; //@date 27.01.2023

import io.github.proto4j.crypto.ICipher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class DefaultBuilder extends AbstractESABuilder<DefaultBuilder> {

    public DefaultBuilder() {
        setCipher(ICipher.newDefaultInstance());
    }

    @Override
    public ESA finish() {
        Objects.requireNonNull(configuration, "config");
        Objects.requireNonNull(keyProvider, "provider");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(cipher, "cipher");

        ESA jar = new DefaultESA(keyProvider, cipher, configuration);

        if (!(content instanceof ESAFile)) {
            throw new ClassFormatError("Invalid output object of type " + content.getClass().getName());
        }

        try {
            jar.load((ESAFile) content);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return jar;
    }

    @Override
    protected DefaultBuilder this0() {
        return this;
    }
}
