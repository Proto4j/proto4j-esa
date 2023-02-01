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

package io.github.proto4j.esa;//@date 23.01.2023

import io.github.proto4j.crypto.ICipher;
import io.github.proto4j.esa.annotation.Output;

/**
 * This tagging interface will be used on classes annotated with {@link Output}
 * as their base class.
 *
 * @see ICipher
 */
public interface ESAFile {

    /**
     * Returns the ESA content as a {@code String}.
     *
     * @return the embedded shared archive file
     */
    public abstract String getEncoded();

    /**
     * The ESA filename to use when exporting it.
     *
     * @return the filename
     */
    public abstract String getFilename();
}
