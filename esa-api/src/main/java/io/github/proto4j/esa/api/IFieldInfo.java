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

package io.github.proto4j.esa.api;//@date 24.01.2023

import org.objectweb.asm.Type;

import java.lang.reflect.Member;

public interface IFieldInfo extends Member {

    public static IFieldInfo getInstance(Type type, String name, Object value, int modifiers) {
        return new DefaultFieldInfo(type, name, modifiers, value);
    }

    public static EncryptedFieldInfo getEncryptedInstance(Type type, String name, Object value, int modifiers) {
        return new DefaultEncryptedFieldInfo(type, name, modifiers, value);
    }

    public default String getDescriptor() {
        return getType().getDescriptor();
    }

    public abstract Object getValue();

    public abstract Type getType();

    public default Class<?> getDeclaringClass() {
        return null;
    }

}
