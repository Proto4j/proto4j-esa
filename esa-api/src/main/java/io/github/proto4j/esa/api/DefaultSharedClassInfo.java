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

import org.objectweb.asm.Type;

class DefaultSharedClassInfo extends DefaultClassInfo
        implements ISharedClassInfo {

    public DefaultSharedClassInfo(Type type, int modifiers, RelocateDetails relocateDetails,
                                  boolean shadowed, boolean output) {
        super(type, modifiers);
        this.relocateDetails = relocateDetails;
        this.shadowed        = shadowed;
        this.output          = output;
    }

    private final RelocateDetails relocateDetails;
    private final boolean         shadowed;
    private final boolean         output;


    @Override
    public boolean isShadowed() {
        return shadowed;
    }

    @Override
    public RelocateDetails getRelocateDetails() {
        return relocateDetails;
    }

    @Override
    public boolean isOutputClass() {
        return output;
    }
}
