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

package io.github.proto4j.crypto.provider; //@date 27.01.2023

public abstract class NativeKeyProvider extends KeyProvider {

    private final String libName;

    public NativeKeyProvider(String libName) {
        this.libName = libName;
        if (!loadLibrary()) {
            throw new IllegalStateException("Could not load library with name: " + libName);
        }
    }

    public boolean loadLibrary() {
        try {
            System.loadLibrary(libName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLibraryName() {
        return libName;
    }
}
