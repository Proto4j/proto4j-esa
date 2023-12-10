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

import org.objectweb.asm.*;
import io.github.proto4j.esa.ESAFile;
import io.github.proto4j.esa.ESA;
import io.github.proto4j.esa.api.asm.IClassCreator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.objectweb.asm.Opcodes.*;

public final class SharedJarClassWriter implements IClassCreator {

    private static final Type   STRING      = Type.getType(String.class);
    private static final String DESCRIPTOR  = "()" + STRING.getDescriptor();
    private static final Type   contentType = Type.getType(ESAFile.class);

    private boolean    exists;
    private IClassInfo info;
    private int version;

    private InputStream source;

    public SharedJarClassWriter() {
        this(V13);
    }

    public SharedJarClassWriter(int version) {
        this.version = version;
    }

    private void implementMethod(MethodVisitor mv, String fieldName, String descriptor,
                                 int size) {
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, info.getType().getInternalName(), fieldName, descriptor);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(size, 1);
        mv.visitEnd();
    }

    @Override
    public void transferTo(OutputStream stream) throws IOException {
        if (classExistent()) {
            ClassReader cr;
            if (source != null) cr = new ClassReader(source);
            else cr = new ClassReader(info.getType().getInternalName());

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            cr.accept(new ExistentClassVisitor(cw), ClassReader.EXPAND_FRAMES);
            stream.write(cw.toByteArray());
            return;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        version = V13;
        cw.visit(version, ACC_PUBLIC + ACC_FINAL,
                 info.getType().getInternalName(), null, "java/lang/Object",
                 new String[]{contentType.getInternalName()});

        for (IFieldInfo info : info.getDeclaredFields()) {
            String value = (String) info.getValue();
            cw.visitField(info.getModifiers(),
                          info.getName(), info.getDescriptor(), null, value);
            if (info.getName().equals(ESA.ENCODED)) {
                implementEncoded(cw, info.getName(), value.length());
            } else if (info.getName().equals(ESA.NAME)) {
                implementFilename(cw, info.getName(), info.getName().length());
            }
        }

        cw.visitEnd();
        stream.write(cw.toByteArray());
    }

    private void implementEncoded(ClassVisitor cw, String name, int size) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC, "getEncoded", DESCRIPTOR,
                null, new String[0]);

        implementMethod(mv, name, STRING.getDescriptor(), size);
    }

    private void implementFilename(ClassVisitor cw, String name, int size) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC, "getFilename", DESCRIPTOR,
                null, new String[0]);

        implementMethod(mv, name, STRING.getDescriptor(), size);
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean classExistent() {
        return exists;
    }

    @Override
    public void setSource(InputStream source) {
        this.source = source;
    }

    @Override
    public void setClassInfo(IClassInfo info) {
        this.info = info;
    }

    private final class ExistentClassVisitor extends ClassVisitor {
        public ExistentClassVisitor(ClassVisitor cv) {
            super(ASM9, cv);
        }


        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            for (String i : interfaces) {
                if (i.equals(contentType.getDescriptor())) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    setFields();
                    return;
                }
            }

            String[] values = new String[interfaces.length + 1];
            System.arraycopy(interfaces, 0, values, 0, interfaces.length);

            values[interfaces.length] = contentType.getInternalName();
            super.visit(version, access, name, signature, superName, values);
            setFields();
        }

        private boolean fieldsSet = false;

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (!fieldsSet) {
                setFields();
            }
            for (IFieldInfo field : info.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return null;
                }
            }
            return super.visitField(access, name, descriptor, signature, value);
        }



        private void setFields() {
            for (IFieldInfo field : info.getDeclaredFields()) {
                super.visitField(field.getModifiers(), field.getName(),
                                 field.getType().getDescriptor(), null,
                                 field.getValue());
                if (field.getName().equals(ESA.NAME)) {
                    implementFilename(getDelegate(), ESA.NAME, field.getName().length());
                } else if (field.getName().equals(ESA.ENCODED)) {
                    implementEncoded(getDelegate(), ESA.ENCODED, ((String)field.getValue()).length());
                }
            }
            fieldsSet = true;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                         String[] exceptions) {
            if (name.equals("getEncoded") && descriptor.equals(DESCRIPTOR)) {
                return null;
            } else if (name.equals("getFilename") && descriptor.equals(DESCRIPTOR)) {
                return null;
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

    }
}
