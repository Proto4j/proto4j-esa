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

public interface IClassInfo extends Member {

    public static IClassInfo getInstance(Type type, int modifiers) {
        return new DefaultClassInfo(type, modifiers);
    }

    static ISharedClassInfo getSharedInstance(
            Type type, int mod, boolean shadowed) {
        return getSharedInstance(type, mod, shadowed, null, false);
    }

    public static ISharedClassInfo getSharedInstance(
            Type type, int mod, boolean shadowed, RelocateDetails details,
            boolean outputClass) {
        return new DefaultSharedClassInfo(type, mod, details, shadowed, outputClass);
    }

    public Type getType();

    public /*NotNull*/ IFieldInfo[] getDeclaredFields();

    @Override
    default Class<?> getDeclaringClass() {
        return null;
    }

    public default String getSimpleName() {
        String name = getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    default String getName() {
        return getType().getClassName();
    }
}
