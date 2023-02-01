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
import io.github.proto4j.esa.api.AbstractClassInfo;
import io.github.proto4j.esa.api.EncryptedFieldInfo;
import io.github.proto4j.esa.api.IFieldInfo;
import io.github.proto4j.esa.api.SharedJarClassWriter;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

/**
 * Specifies the class that will store the contents of an ESA file. It is
 * recommended to annotate only one class with this annotation as only one
 * class will be used as the final output.
 * <p>
 * The {@link SharedJarClassWriter} will use any existing information stored
 * in the initial output class and write it to the final output class. Additionally,
 * two fields will be created (both are static and final as well as public):
 * <ul>
 *     <li><code>filename</code>: The name of the stored ESA-file</li>
 *     <li><code>encodedJar</code>: the ESA file content</li>
 * </ul>
 * Thus, an existing class like the following:
 * <pre>
 *     &#064;Output
 *     class Foo {}
 * </pre>
 * would be transformed into the following output:
 * <pre>
 *     public final class Foo implements ESAFile {
 *         // These fields will be generated
 *         public static final String filename = "...";
 *         public static final String encodedJar = "...";
 *
 *         &#064;Override  // automatic method
 *         public String getFilename() {return filename;}
 *
 *         // The needed stack size for this method will be calculated as well
 *         &#064;Override
 *         public String getEncoded() {return encodedJar;}
 *     }
 * </pre>
 * @see SharedJarClassWriter
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Output {

    /**
     * Special <code>IClassInfo</code> for the output class storing only the
     * necessary fields.
     *
     * @see Output
     */
    public static final class OutputClassInfo extends AbstractClassInfo {

        /**
         * Creates a new <code>OutputClassInfo</code> with the given existing
         * output class type.
         *
         * @param cls the output class type
         * @param filename the filename to use
         * @param data the encrypted field info
         */
        public OutputClassInfo(Class<?> cls, String filename, EncryptedFieldInfo data) {
            this(Type.getType(cls), filename, data);
        }

        /**
         * Creates a new <code>OutputClassInfo</code> with the given existing
         * output class type information.
         *
         * @param type the output class type
         * @param filename the filename to use
         * @param data the encrypted field info
         */
        public OutputClassInfo(Type type, String filename, EncryptedFieldInfo data) {
            super(type);
            IFieldInfo field = IFieldInfo.getInstance(
                    Type.getType(String.class), ESA.NAME,
                    filename, Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC);
            // Only two fields are allowed
            setFields(new IFieldInfo[]{ field, data });
        }

        /**
         * Returns the Java language modifiers for the output class - by default
         * <code>public</code> and <code>final</code>.
         *
         * @return the class modifiers
         */
        @Override
        public int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }
    }

}
