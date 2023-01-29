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
import io.github.proto4j.crypto.EncryptionWrapper;

final class DefaultEncryptedFieldInfo extends DefaultFieldInfo
        implements EncryptedFieldInfo {

    private Object encrypted;

    public DefaultEncryptedFieldInfo(Type type, String name, int modifiers, Object value) {
        super(type, name, modifiers, value);
    }

    @Override
    public Object getEncrypted() {
        return encrypted;
    }

    @Override
    public Object getValue() {
        if (encrypted != null) {
            return getEncrypted();
        }
        return super.getValue();
    }

    @Override
    public void encrypt(EncryptionWrapper<Object, Object> mapper) throws Exception {
        if (mapper != null && encrypted == null) {
            this.encrypted = mapper.encrypt(getValue());
        }
    }
}
