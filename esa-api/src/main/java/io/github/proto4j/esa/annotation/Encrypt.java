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

package io.github.proto4j.esa.annotation;//@date 23.01.2023

import io.github.proto4j.esa.ESA;
import io.github.proto4j.esa.api.asm.StaticBlockWriter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used on fields (static and final) to indicate their content
 * should be encrypted at runtime.
 * <p>
 * For more information about how the encryption is implemented with this
 * annotation, see {@link ESA#wrap()} and {@link StaticBlockWriter}.
 * <p>
 * To <i>encrypt</i> a field's value, the initial value must be the result of
 * {@code ESA#wrap()} and the field has to be static and final.
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Encrypt {
    /**
     * The value that needs to be encrypted at runtime.
     *
     * @return the plain text value
     */
    String value();

}
