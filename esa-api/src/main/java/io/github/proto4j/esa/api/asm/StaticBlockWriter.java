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

import io.github.proto4j.esa.ESA;
import io.github.proto4j.esa.api.EncryptedFieldInfo;
import io.github.proto4j.esa.api.IFieldInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A <code>StaticBlockWriter</code> is used to transform the bytecode of a java
 * class file, especially the <code>clinit</code>-blocks.
 * <p>
 * This method visitor tries to remove the {@link ESA#wrap()} statement
 * and turn it into the encrypted value by its field info. Actually, there is
 * only one small change in the bytecode:
 * <pre>
 *     // Instead of
 *     INVOKESTATIC ESA, wrap, ()V;, false
 *     // there will be an lcd statement
 *     LDC "encrypted string"
 * </pre>
 *
 * @see ESA#wrap()
 */
public final class StaticBlockWriter extends MethodVisitor {

    /**
     * The static initializer block identifier
     */
    public static final String CLINIT = "<clinit>";

    /**
     * The fields to change
     */
    private final IFieldInfo[] fields;

    /**
     * Creates a new block writer that should edit the provided fields.
     *
     * @param details the fields to be edited
     * @param visitor the delegating visitor
     */
    public StaticBlockWriter(IFieldInfo[] details, MethodVisitor visitor) {
        super(Opcodes.ASM9, visitor);
        this.fields = details;
    }

    /**
     * {@inheritDoc}
     *
     * @param opcode {@inheritDoc}
     * @param owner {@inheritDoc}
     * @param name {@inheritDoc}
     * @param descriptor {@inheritDoc}
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (opcode == Opcodes.PUTSTATIC) {
            // Here we're inserting an extra instruction that puts our hardcoded
            // and hopefully encrypted value onto the stack. Everything else is
            // ready.
            for (IFieldInfo ed : fields) {
                if (ed instanceof EncryptedFieldInfo && ed.getName().equals(name)) {
                    visitLdcInsn(ed.getValue());
                    break;
                }
            }
        }
        // Finally, the instruction that assigns our value or the value by
        // the previous instructions to the static field.
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    /**
     * {@inheritDoc}
     *
     * @param opcode {@inheritDoc}
     * @param owner {@inheritDoc}
     * @param name {@inheritDoc}
     * @param descriptor {@inheritDoc}
     * @param isInterface {@inheritDoc}
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        // All method calls to ESA.wrap() will be removed. The hardcoded
        // values will be inserted instead.
        if (!name.equals("wrap") && !owner.equals(ESA.TYPE.getInternalName())) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
