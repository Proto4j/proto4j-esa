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

package io.github.proto4j.esa.api; //@date 27.01.2023

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class ByteCodeClassLoader<T> extends ClassLoader {

    public static final class ClassByteCode {
        public final byte[] content;

        public Class<?> loadedClass;

        ClassByteCode(byte[] content) {this.content = content;}
    }

    private final Map<T, ClassByteCode> classMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    protected abstract Class<?> findClass(String name) throws ClassNotFoundException;

    public final void put(T type, byte[] content) throws IllegalAccessError {
        if (classMap.containsKey(type)) {
            throw new IllegalAccessError("Type: " + type + "already defined!");
        }

        classMap.put(type, new ClassByteCode(content));
    }

    public final boolean remove(T type) {
        return classMap.remove(type) != null;
    }

    protected ClassByteCode get(T type) {
        return classMap.getOrDefault(type, null);
    }

    protected ClassByteCode get(Predicate<? super T> filter) {
        for (T key : classMap.keySet()){
            if (filter.test(key)) {
                return classMap.get(key);
            }
        }
        return null;
    }

}
