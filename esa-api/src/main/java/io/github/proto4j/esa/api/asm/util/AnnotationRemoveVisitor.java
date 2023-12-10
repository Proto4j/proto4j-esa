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

package io.github.proto4j.esa.api.asm.util; //@date 23.01.2023

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;

/**
 * Simple {@code FieldVisitor} to remove the provided annotation.
 *
 * @param <A> the annotation type to remove
 */
public class AnnotationRemoveVisitor<A extends Annotation> extends FieldVisitor {

    /**
     * The annotation type
     */
    private final Class<A> cls;

    /**
     * The internal type object used to identify the annotation
     */
    private final Type type;

    /**
     * Creates a new visitor with the given delegate and annotation type.
     *
     * @param cls the annotation type to look for
     * @param fv the delegate visitor (can be null)
     */
    public AnnotationRemoveVisitor(Class<A> cls, FieldVisitor fv) {
        super(Opcodes.ASM9, fv);
        this.cls = cls;
        this.type = Type.getType(getAnnotationType());
    }

    /**
     * {@inheritDoc}
     *
     * @param descriptor the class descriptor of the annotation class.
     * @param visible {@literal true} if the annotation is visible at runtime.
     * @return {@code null} if the descriptor matches the annotation type's
     *         descriptor.
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(getType().getDescriptor())) {
            return null;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    /**
     * Returns the internal type representation of the annotation type
     *
     * @return the internal type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the annotation type to remove
     *
     * @return the annotation type
     */
    public Class<A> getAnnotationType() {
        return cls;
    }
}
