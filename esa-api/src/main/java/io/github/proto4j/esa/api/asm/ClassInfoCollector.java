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

import io.github.proto4j.esa.api.*;
import org.objectweb.asm.*;
import io.github.proto4j.esa.annotation.Encrypt;
import io.github.proto4j.esa.annotation.Output;
import io.github.proto4j.esa.annotation.Relocate;
import io.github.proto4j.esa.annotation.Shadow;
import io.github.proto4j.esa.api.asm.util.TypedAnnotationVisitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassInfoCollector extends ClassVisitor {

    public static final Type SHADOWED_TYPE  = Type.getType(Shadow.class);
    public static final Type RELOCATE_TYPE  = Type.getType(Relocate.class);
    public static final Type ENCRYPTED_TYPE = Type.getType(Encrypt.class);
    public static final Type OUTPUT_TYPE = Type.getType(Output.class);

    private Set<EncryptedFieldInfo> encryptedDetails;

    private RelocateDetails relocateDetails;

    private boolean shadowed;
    private boolean output;
    private String  name;

    private volatile ISharedClassInfo classInfo;

    private int modifiers;

    public static ClassInfoCollector collect(ClassReader reader) {
        return collect(reader, null);
    }

    public static ClassInfoCollector collect(ClassReader reader, ClassVisitor cv) {
        ClassInfoCollector collector = new ClassInfoCollector(Opcodes.ASM9, cv);
        reader.accept(collector, 0);
        return collector;
    }


    protected ClassInfoCollector(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.name      = name;
        this.shadowed  = false;
        this.modifiers = access;

        relocateDetails  = null;
        encryptedDetails = new HashSet<>();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(SHADOWED_TYPE.getDescriptor())) {
            shadowed = true;
        } else if (descriptor.equals(RELOCATE_TYPE.getDescriptor())) {
            return new TypedAnnotationVisitor<>(Relocate.class, new RelocateFinishAction());
        } else if (descriptor.equals(OUTPUT_TYPE.getDescriptor())) {
            output = true;
        }

        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return new EncryptionAwareVisitor(name, descriptor, value, access);
    }

    public synchronized ISharedClassInfo getClassInfo() {
        if (classInfo != null) {
            return classInfo;
        }

        classInfo = IClassInfo.getSharedInstance(
                Type.getType('L' + name + ';'), modifiers, shadowed, relocateDetails, output);

        if (classInfo instanceof AbstractClassInfo) {
            IFieldInfo[] fields = new IFieldInfo[encryptedDetails.size()];
            int i = 0;
            for (IFieldInfo info : encryptedDetails) {
                fields[i++] = info;
            }
            ((AbstractClassInfo) classInfo).setFields(fields);
        }
        return classInfo;
    }

    protected Set<EncryptedFieldInfo> getEncryptedDetails() {
        return encryptedDetails;
    }

    protected RelocateDetails getRelocateDetails() {
        return relocateDetails;
    }

    public String getName() {
        return name;
    }

    public boolean isShadowed() {
        return shadowed;
    }

    public boolean isOutputClass() {
        return output;
    }

    private class RelocateFinishAction implements TypedAnnotationVisitor.FinishAction {

        @Override
        public void apply(Map<String, Object> values) {
            if (values.size() == 0) {
                return;
            }

            relocateDetails = RelocateDetails.get(
                    (String) values.getOrDefault("to", null),
                    (Boolean) values.getOrDefault("recursive", false));
        }
    }

    private class EncryptionAwareVisitor extends FieldVisitor implements TypedAnnotationVisitor.FinishAction {
        private final String name;
        private final Object value;
        private final Type   type;
        private final int    mod;

        private String content;

        private boolean encrypted = false;

        public EncryptionAwareVisitor(String name, String descriptor, Object value, int mod) {
            super(Opcodes.ASM9);
            this.name  = name;
            this.value = value;
            this.type  = Type.getType(descriptor);
            this.mod   = mod;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // Collect information about the encryption and what needs to be encrypted
            if (descriptor.equals(ENCRYPTED_TYPE.getDescriptor())) {
                encrypted = true;
                return new TypedAnnotationVisitor<>(Encrypt.class, this);
            }
            return null;
        }

        public boolean isEncrypted() {
            return encrypted;
        }

        @Override
        public void visitEnd() {
            if (isEncrypted()) {
                EncryptedFieldInfo info = IFieldInfo.getEncryptedInstance(
                        type, name, content != null ? content : value, mod);
                encryptedDetails.add(info);
            }
        }

        @Override
        public void apply(Map<String, Object> values) {
            if (values == null) {
                return;
            }

            this.content = (String) values.getOrDefault("value", null);
        }
    }
}
