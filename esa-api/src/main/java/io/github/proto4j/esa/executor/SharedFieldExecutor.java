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

package io.github.proto4j.esa.executor; //@date 23.01.2023

import io.github.proto4j.esa.*;

import java.lang.reflect.Field;

public class SharedFieldExecutor<T> extends SharedExecutor<T> {

    private final Class<T> returnType;

    protected String targetClassName;
    protected String targetFieldName;

    protected boolean recursiveLookup;
    protected Object parent;

    public SharedFieldExecutor(ESA esa, Class<T> returnType,
                               String targetClassName, String targetFieldName,
                               boolean recursiveLookup) {
        this(esa, returnType, null, targetClassName, targetFieldName, recursiveLookup);
    }

    public SharedFieldExecutor(ESA esa, Class<T> returnType, Object parent,
                               String targetClassName, String targetFieldName,
                               boolean recursiveLookup) {
        super(esa);
        this.parent = parent;
        this.returnType = returnType;
        this.targetClassName = targetClassName;
        this.targetFieldName = targetFieldName;
        this.recursiveLookup = recursiveLookup;
    }

    public SharedFieldExecutor(ESA jar, Class<T> returnType) {
        super(jar);
        this.returnType = returnType;
    }

    protected Object get(Field field) throws Exception {
        return field.get(parent);
    }

    @Override
    public T call() throws SharedException {
        if (targetClassName == null || targetFieldName == null) {
            throw new IllegalArgumentException("target.class || target.name == null");
        }

        if (returnType == null) {
            throw new IllegalArgumentException("returnType == null");
        }

        try {
            Field field = getArchive().getSharedField(
                    targetClassName, targetFieldName, recursiveLookup);

            if (field != null) {
                 field.setAccessible(true);
                 Object value = get(field);
                 if (returnType.isInstance(value)) {
                     return returnType.cast(value);
                 }
            }
        } catch (NoSuchSharedFieldException | NoSuchSharedClassException e) {
            throw new NoSuchTargetException(e);
        } catch (Exception e) {
            throw new SharedInvocationException(e);
        }

        return null;
    }

}
