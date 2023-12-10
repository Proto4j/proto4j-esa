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

import org.objectweb.asm.Type;

public final class TypeClassLoader extends ByteCodeClassLoader<Type> {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Assuming we get the internal name, which contains '.' instead
        // of '/'. The ';' will be added at the end:
        final String descriptor = 'L' + name.replaceAll("\\.", "/") + ';';
        ClassByteCode byteCode = get(key -> key.getDescriptor().equals(descriptor));

        if (byteCode == null) {
            throw new ClassNotFoundException("Could not find class: " + name);
        }
        else if (byteCode.loadedClass != null) {
            return byteCode.loadedClass;
        }

        Class<?> cls =  super.defineClass(name, byteCode.content, 0, byteCode.content.length);
        return (byteCode.loadedClass = cls);
    }
}
