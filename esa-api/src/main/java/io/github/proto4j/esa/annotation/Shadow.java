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

package io.github.proto4j.esa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes that will be <i>shared</i> among the application should be annotated
 * with <code>@Shadow</code>. Before running the application, classes with this
 * annotation are removed from the original classpath and will be wrapped into
 * an ESA file.
 * <p>
 * By now, it is not possible to use this annotation on inner classes. Therefore,
 * it is recommended to decide whether it is necessary to include the annotated
 * class in the ESA file.
 * <p>
 * To interact with shared classes, the fully qualified name is needed - make sure
 * you don't pass <code>Foo.class.getName()</code> as the class will be invisible
 * at runtime which raises errors on this call.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Shadow {}
