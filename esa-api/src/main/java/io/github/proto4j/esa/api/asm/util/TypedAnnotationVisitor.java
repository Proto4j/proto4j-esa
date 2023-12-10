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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import javax.swing.event.EventListenerList;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Class visitors that want to collect data by an annotation, may use this class
 * to do so. It provides a way to listen to the collected values, so the usage
 * can be quite simple:
 * <pre>
 *  class MyVisitor extends {@link ClassVisitor} {
 *     public MyVisitor() {
 *         super(ASM);
 *
 *     &#064;Override
 *     public {@link AnnotationVisitor} visitAnnotation(String descriptor, boolean visible) {
 *         Type myAnnotationType = Type.getType(MyAnnotation.class);
 *         // Validate if it is the annotation we are looking for
 *         if (descriptor.equals(myAnnotationType.getDescriptor())) {
 *             return new TypedAnnotationVisitor&lt;&gt;(MyAnnotation.class).onFinish(map -&gt; {
 *                   // handle the collected values...
 *             });
 *         }
 *         return super.visitAnnotation(descriptor, visible);
 *     }
 *  }
 * </pre>
 * It is not necessary to provide a <code>FinishAction</code>, the collected values
 * can be retrieved afterwards.
 *
 * @param <A> the annotation type
 */
public class TypedAnnotationVisitor<A extends Annotation> extends AnnotationVisitor {

    /**
     * The listener interface for receiving the last action event. The class that
     * is interested in processing the discovered annotation values implements
     * this interface.
     *
     * @see TypedAnnotationVisitor
     */
    public interface FinishAction {
        void apply(Map<String, Object> values);
    }

    /**
     * The annotation type to identify this collector
     */
    private final Class<A> annotationType;

    /**
     * The collected annotation values
     */
    private final Map<String, Object> values = new HashMap<>();

    /**
     * The optional finish action.
     *
     * @apiNote This action should be replaced by an {@link EventListenerList}
     *         in future releases.
     */
    private FinishAction action;

    /**
     * Creates a new typed visitor.
     *
     * @param annotationType the annotation type to use
     */
    public TypedAnnotationVisitor(Class<A> annotationType) {
        this(annotationType, null);
    }

    /**
     * Creates a new typed visitor and sets the provided finish action.
     *
     * @param annotationType the type to use
     * @param action the action that should be executed on {@link #visitEnd()}
     */
    public TypedAnnotationVisitor(Class<A> annotationType, FinishAction action) {
        super(Opcodes.ASM9);
        this.annotationType = annotationType;
        this.action = action;
    }

    public void clear() {
        values.clear();
    }

    /**
     * Sets the <code>FinishAction</code> for this visitor. Additionally, this
     * method returns this object.
     *
     * @param action the action to execute on {@link #visitEnd()}
     * @return this object
     */
    public TypedAnnotationVisitor<A> onFinish(FinishAction action) {
        setAction(action);
        return this;
    }

    /**
     * Sets the <code>FinishAction</code> for this visitor.
     *
     * @param action the action to execute on {@link #visitEnd()}
     */
    public void setAction(FinishAction action) {
        this.action = action;
    }

    /**
     * {@inheritDoc}
     *
     * @param name {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);
        values.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
        if (action != null) {
            action.apply(values);
            clear();
        }
    }

    /**
     * Returns the annotation type for this visitor.
     *
     * @return the identifying annotation type
     */
    public Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * Returns the collected values.
     *
     * @return the annotation's values
     * @apiNote The returned {@link Map} contains only primitive objects
     */
    public Map<String, Object> getValues() {
        return values;
    }
}
