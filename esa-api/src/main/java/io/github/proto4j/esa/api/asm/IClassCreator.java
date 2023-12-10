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

package io.github.proto4j.esa.api.asm; //@date 24.01.2023

import io.github.proto4j.esa.api.IClassInfo;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>IClassCreators</code> are necessary to transform/edit the bytecode of
 * an existing Java class.
 * <p>
 * Implementations of this interface may transform the bytecode directly, but
 * also should implement a secondary source transformation.
 *
 * @see io.github.proto4j.esa.api.SharedJarClassWriter
 */
public interface IClassCreator {

    /**
     * Applies the information given in the stored <code>IClassInfo</code> and
     * writes the transformed bytecode to the given <code>OutputStream</code>.
     * <p>
     * Although, calling <code>new {@link ClassReader}()</code> with the class'
     * name instead of an <code>InputStream</code> to its class file may result
     * in issues when invoking this method outside the original context.
     *
     * @param stream the destination
     * @throws IOException if an I/O Error occurs
     */
    public abstract void transferTo(OutputStream stream) throws IOException;

    /**
     * Applies a new <code>IClassInfo</code> to this creator.
     *
     * @param info the info to use
     */
    public abstract void setClassInfo(IClassInfo info);

    /**
     * Sets the source stream where the initial bytecode is stored.
     *
     * @param source the class' source
     */
    public abstract void setSource(InputStream source);

}
