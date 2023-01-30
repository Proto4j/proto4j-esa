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
import io.github.proto4j.esa.SharedJar
import io.github.proto4j.esa.annotation.Output
import io.github.proto4j.esa.api.*
import io.github.proto4j.esa.api.asm.ClassInfoCollector
import io.github.proto4j.esa.api.asm.ClassInfoWriter
import io.github.proto4j.esa.api.asm.IClassCreator
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type

import javax.annotation.Nonnull
import javax.annotation.Nullable
import javax.crypto.Cipher
import javax.crypto.SecretKey
import java.lang.reflect.Modifier

final class APIUtil {

    public static final ICipher cipher = ICipher.getInstance()

    @Nullable
    static IClassInfo getOutputClassInfo(Class<?> cls, String filename, byte[] encryptedJar) {
        return getOutputClassInfo(Type.getType(cls), filename, encryptedJar)
    }

    @Nullable
    static IClassInfo getOutputClassInfo(Type cls, String filename, byte[] encryptedJar) {
        if (cls == null || encryptedJar == null || encryptedJar.length == 0) {
            return null
        }

        EncryptedFieldInfo field = IFieldInfo.getEncryptedInstance(
                Type.getType(String.class), SharedJar.ENCODED,
                new String(encryptedJar), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL
        )
        return new Output.OutputClassInfo(cls, filename, field)
    }

    @Nonnull
    static IClassCreator getOutputClassCreator(IClassInfo classInfo, boolean exists) throws IOException {
        if (classInfo == null) {
            throw new IllegalArgumentException("classInfo == null")
        }

        SharedJarClassWriter cw = new SharedJarClassWriter()
        cw.setClassInfo(classInfo)
        cw.setExists(exists)
        return cw
    }

    @Nonnull
    static ISharedClassInfo inspect(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("is == null")
        }

        ClassReader cr = new ClassReader(is)
        return ClassInfoCollector.collect(cr).getClassInfo()
    }

    static boolean writeClass(ISharedClassInfo classInfo, InputStream src, OutputStream dest) throws IOException {
        if (classInfo == null || src == null || dest == null) {
            return false
        }

        ClassReader cr = new ClassReader(src)
        ClassInfoWriter writer = new ClassInfoWriter(classInfo, new ClassWriter(ClassWriter.COMPUTE_MAXS))

        cr.accept(writer, 0)
        byte[] bytes = writer.getBytes()
        if (bytes.length == 0) {
            return false
        }
        dest.write(bytes)
        dest.flush()
        return true
    }

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

    static byte[] encryptJar(byte[] content, SecretKey key) {
        if (content == null || content.length == 0 || key == null) {
            return new byte[0]
        }

        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(content)

    }

    static void writeOutputClass(String path, String name, String filename,
                                 byte[] encryptedJar, Type outputClass) {
        File output = new File(path, name)
        boolean existent = output.exists()

        IClassInfo info = getOutputClassInfo(outputClass, filename, encryptedJar)
        IClassCreator cc = getOutputClassCreator(info, existent)

        if (!existent) {
            new File(path).mkdirs()
            output.createNewFile()

            try (OutputStream os = output.newOutputStream()) {
                cc.transferTo(os)
            }
        } else {
            // First, create the output stream that will store the bytes of the
            // output file temporarily
            try (OutputStream fos = new ByteArrayOutputStream()) {

                try (InputStream is = output.newInputStream()) {
                    cc.setSource(is)
                    cc.transferTo(fos)
                }

                try (OutputStream oos = output.newOutputStream()) {
                    IOUtils.copy(new ByteArrayInputStream(fos.toByteArray()), oos)
                }

                int length = fos.size()
                if (length != 0) {
                    fos.reset()
                    fos.write(new byte[length])
                }
            }
        }

    }

}
