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

import io.github.proto4j.esa.NoSuchTargetException;
import io.github.proto4j.esa.SharedException;
import io.github.proto4j.esa.ESA;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SharedMethodExecutor<T> extends SharedExecutor<T> {

    private final Class<T> returnType;

    protected String targetMethodName;
    protected String targetClassName;

    protected Class<?>[] argumentTypes;

    public SharedMethodExecutor(ESA esa, Class<T> returnType) {
        super(esa);
        this.returnType = returnType;
    }

    public SharedMethodExecutor(ESA esa, Class<T> returnType,
                                String targetMethodName, String targetClassName,
                                Class<?>[] argumentTypes) {
        super(esa);
        this.returnType = returnType;
        this.targetMethodName = targetMethodName;
        this.targetClassName = targetClassName;
        this.argumentTypes = argumentTypes;
    }

    protected Object invokeMethod(Method target) throws Exception {
        return target.invoke(null);
    }

    private volatile Method target;

    @Override
    public final synchronized T call() throws SharedException {
        if (targetClassName == null || targetMethodName == null) {
            throw new IllegalArgumentException("target.method || target.class == null");
        }

        if (argumentTypes == null) {
            throw new IllegalArgumentException("target.argTypes == null");
        }

        if (returnType == null) {
            throw new IllegalArgumentException("returnType == null");
        }

        if (target == null) try {
            target = getArchive().getSharedMethod(
                    targetClassName, targetMethodName, argumentTypes);
        } catch (Exception e) {
            throw new InvocationException(e);
        }

        if (target == null) {
            throw new NoSuchTargetException("Could not resolve target method");
        }

        try {
            target.setAccessible(true);
            Object result = invokeMethod(target);

            if (returnType.isInstance(result)) {
                return returnType.cast(result);
            }
            else if (result != null) {
                throw new ClassFormatError("Invalid return type, expected: " + returnType.getName());
            }

        } catch (InvocationTargetException e) {
            throw new InvocationException(e.getTargetException());
        } catch (Exception e) {
            throw new SharedInvocationException(e);
        }

        return null;
    }
}
