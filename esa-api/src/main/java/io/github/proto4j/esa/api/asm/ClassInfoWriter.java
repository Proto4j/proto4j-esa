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

package io.github.proto4j.esa.api.asm; //@date 23.01.2023

import io.github.proto4j.esa.annotation.Encrypt;
import org.objectweb.asm.*;
import io.github.proto4j.esa.api.EncryptedFieldInfo;
import io.github.proto4j.esa.api.IFieldInfo;
import io.github.proto4j.esa.api.ISharedClassInfo;
import io.github.proto4j.esa.api.asm.util.AnnotationRemoveVisitor;

import static io.github.proto4j.esa.api.asm.StaticBlockWriter.CLINIT;

public class ClassInfoWriter extends ClassVisitor {

    /**
     * The class info that is applied to the content
     */
    private final ISharedClassInfo classInfo;

    /**
     * The class writer that will create the finalized bytecode
     */
    private final ClassWriter cw;

    /**
     * Creates a new <code>ClassInfoWriter</code> that applies the information
     * stored in the provided <code>ISharedClassInfo</code>.
     *
     * @param classInfo the class info that stores the necessary information
     * @param cv the delegating visitor
     */
    public ClassInfoWriter(ISharedClassInfo classInfo, ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
        this.classInfo = classInfo;
        if (cv instanceof ClassWriter) {
            cw = (ClassWriter) cv;
        }
        else {
            cw = null;
        }
    }

    public byte[] getBytes() {
        return cw != null? cw.toByteArray() : new byte[0];
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, classInfo.getType().getInternalName(), signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        if (name.equals(CLINIT)) {
            // handle every static block by replacing the wrap() calls with their
            // encrypted values.
            return new StaticBlockWriter(classInfo.getDeclaredFields(),
                                         super.visitMethod(access, name, descriptor, signature, exceptions));
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        for (IFieldInfo details : classInfo.getDeclaredFields()) {
            if (details instanceof EncryptedFieldInfo && details.getName().equals(name)) {
                // remove @Encrypt annotation with decrypted Text
                FieldVisitor visitor = super.visitField(access, name, descriptor, signature, details.getValue());
                return new AnnotationRemoveVisitor<>(Encrypt.class, visitor);
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }



}
