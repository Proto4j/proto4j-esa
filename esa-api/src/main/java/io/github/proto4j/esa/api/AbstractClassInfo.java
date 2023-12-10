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

package io.github.proto4j.esa.api; //@date 24.01.2023

import org.objectweb.asm.Type;

public abstract class AbstractClassInfo implements IClassInfo {

    private final Type type;

    private volatile IFieldInfo[] fields;

    protected AbstractClassInfo(Type type) {this.type = type;}

    public void setFields(IFieldInfo[] fields) {
        this.fields = fields;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public IFieldInfo[] getDeclaredFields() {
        return fields == null ? new IFieldInfo[0] : fields;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }
}
