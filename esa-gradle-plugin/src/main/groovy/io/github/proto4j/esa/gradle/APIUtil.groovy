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

package io.github.proto4j.esa.gradle

import io.github.proto4j.crypto.ICipher
import io.github.proto4j.esa.ESA
import io.github.proto4j.esa.annotation.Output
import io.github.proto4j.esa.api.*
import io.github.proto4j.esa.api.asm.ClassInfoCollector
import io.github.proto4j.esa.api.asm.ClassInfoWriter
import io.github.proto4j.esa.api.asm.IClassCreator
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.ByteArrayOutputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type

import javax.annotation.Nonnull
import javax.annotation.Nullable
import javax.crypto.Cipher
import javax.crypto.SecretKey
import java.lang.reflect.Modifier

final class APIUtil {

    /**
     * The global cipher instance used to encrypt the JAR file.
     */
    //FIXME: The cipher used for encryption should not be static
    public static final ICipher cipher = ICipher.getInstance()

    /**
     * Creates the output-class details according to the given parameters.
     *
     * @param cls the destination class
     * @param filename the ESA filename
     * @param encryptedJar the ESA file as a byte array
     * @return the created {@code IClassInfo} object or {@code null} on failure
     */
    @Nullable
    static IClassInfo getOutputClassInfo(Class<?> cls, String filename, byte[] encryptedJar) {
        return getOutputClassInfo(Type.getType(cls), filename, encryptedJar)
    }

    /**
     * Creates the output-class details according to the given parameters.
     *
     * @param cls the destination type
     * @param filename the ESA filename
     * @param encryptedJar the ESA file as a byte array
     * @return the created {@code IClassInfo} object or {@code null} on failure
     */
    @Nullable
    static IClassInfo getOutputClassInfo(Type cls, String filename, byte[] encryptedJar) {
        if (cls == null || encryptedJar == null || encryptedJar.length == 0) {
            return null
        }

        EncryptedFieldInfo field = IFieldInfo.getEncryptedInstance(
                Type.getType(String.class), ESA.ENCODED,
                new String(encryptedJar), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL
        )
        return new Output.OutputClassInfo(cls, filename, field)
    }

    /**
     * Creates the class writer that is used to transform the output class.
     *
     * @param classInfo the class info to write
     * @param exists whether the class is existent
     * @return the class creator used to write/ transofrm the output class
     * @throws IllegalArgumentException if the given class info is {@code null}
     */
    @Nonnull
    static IClassCreator getOutputClassCreator(IClassInfo classInfo, boolean exists)
            throws IllegalArgumentException {
        if (classInfo == null) {
            throw new IllegalArgumentException("classInfo == null")
        }

        SharedJarClassWriter cw = new SharedJarClassWriter()
        cw.setClassInfo(classInfo)
        cw.setExists(exists)
        return cw
    }

    /**
     * Gathers information about the class file that is linked to the given input
     * stream.
     *
     * @param is the class file {@code InputStream}
     * @return the shared class info which contains detailed information about
     *          the inspected class.
     * @throws IOException if an I/O error occurs
     */
    @Nonnull
    static ISharedClassInfo inspect(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("is == null")
        }

        ClassReader cr = new ClassReader(is)
        return ClassInfoCollector.collect(cr).getClassInfo()
    }

    /**
     * Applies the provided class info object to the given source and writes the
     * result to the given {@code OutputStream}. The returned value indicates
     * whether this method executed successful.
     *
     * @param classInfo the class info to apply to the source file
     * @param src the class file input stream
     * @param dest the destination stream
     * @return whether this method executed successful
     * @throws IOException if any I/O error occurs
     */
    static boolean writeClass(ISharedClassInfo classInfo, InputStream src, OutputStream dest) throws IOException {
        if (classInfo == null || src == null || dest == null) {
            return false
        }

        ClassReader cr = new ClassReader(src)
        ClassInfoWriter writer = new ClassInfoWriter(classInfo, new ClassWriter(ClassWriter.COMPUTE_MAXS))

        cr.accept(writer, 0)

        // If any error occurs while processing, the writer
        // returns an empty byte array.
        byte[] bytes = writer.getBytes()
        if (bytes.length == 0) {
            return false
        }

        dest.write(bytes)
        // We don't want to close the OutputStream here as there
        // could be some operations that want to write to it later on.
        dest.flush()
        return true
    }

    /**
     * Tries to encrypt all fields that are an instance of {@code EncryptedFieldInfo}
     * by applying the given key to them.
     *
     * @param classInfo the class details storing the fields
     * @param key the key used to encrypt
     */
    static void encryptAll(IClassInfo classInfo, SecretKey key) {
        if (classInfo == null || key == null) {
            return
        }

        for (IFieldInfo fieldInfo in classInfo.declaredFields) {
            if (fieldInfo instanceof EncryptedFieldInfo) {
                fieldInfo.encrypt { value ->
                    if (!(value instanceof String)) return null

                    cipher.init(Cipher.ENCRYPT_MODE, key)
                    return new String(cipher.doFinal(fieldInfo.getValue().toString().getBytes()))
                }
            }
        }
    }

    /**
     * Encrypts the JAR file with the provided secret key.
     *
     * @param content the JAR content
     * @param key the key used to encrypt
     * @return the encrypted JAR file of an empty byte array in invalid arguments
     */
    static byte[] encryptJar(byte[] content, SecretKey key) {
        if (content == null || content.length == 0 || key == null) {
            return new byte[0]
        }

        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(content)

    }

    /**
     * Writes the output class to the desired path. This method is designed
     * to copy any data from existing output classes first before applying
     * the ESA contents to them.
     *
     * @param path the destination directory
     * @param name the destination filename
     * @param filename the ESA filename
     * @param encryptedJar the encryption JAR as raw bytes
     * @param outputClass the destination class type
     */
    static void writeOutputClass(String path, String name, String filename,
                                 byte[] encryptedJar, Type outputClass) {
        File output = new File(path, name)
        boolean existent = output.exists()

        IClassInfo info = getOutputClassInfo(outputClass, filename, encryptedJar)
        IClassCreator cc = getOutputClassCreator(info, existent)

        if (!existent) {
            // Just create the new file and transfer the result
            // of the class creator to it.
            new File(path).mkdirs()
            output.createNewFile()

            try (OutputStream os = output.newOutputStream()) {
                cc.transferTo(os)
            }
        } else {
            // At first, create an OutputStream that will store the
            // transformed class file temporarily and then copy this
            // class to the original file.
            try (OutputStream fos = new ByteArrayOutputStream()) {
                try (InputStream is = output.newInputStream()) {
                    cc.setSource(is)
                    cc.transferTo(fos)
                }

                try (OutputStream oos = output.newOutputStream()) {
                    IOUtils.copy(new ByteArrayInputStream(fos.toByteArray()), oos)
                }
            }
        }

    }

}
