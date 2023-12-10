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

package io.github.proto4j.esa; //@date 28.01.2023

import io.github.proto4j.crypto.provider.KeyProvider;
import io.github.proto4j.crypto.ICipher;

import java.util.Objects;

public abstract class AbstractESABuilder<T extends AbstractESABuilder<T>>
        extends ESA.Builder<T> {

    protected KeyProvider keyProvider;
    protected ICipher cipher;
    protected ClassLoader classLoader;
    protected JarConfiguration configuration;

    protected Object content;

    @Override
    public abstract ESA finish();

    protected abstract T this0();

    @Override
    public T configure(JarConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        return this0();
    }

    @Override
    public T setProvider(KeyProvider provider) {
        this.keyProvider = Objects.requireNonNull(provider);
        return this0();
    }

    @Override
    public T setOutputClass(Class<?> cls) {
        if (!cls.isAssignableFrom(ESAFile.class)) {
            throw new ClassFormatError("Invalid base class of: " + cls.getName());
        }

        try {
            content = cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return this0();
    }

    @Override
    public T setOutputObject(Object outputObject) {
        this.content = Objects.requireNonNull(outputObject);
        return this0();
    }

    @Override
    public T setClassLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
        return this0();
    }

    @Override
    public T setCipher(ICipher cipher) {
        this.cipher = cipher;
        return this0();
    }
}
