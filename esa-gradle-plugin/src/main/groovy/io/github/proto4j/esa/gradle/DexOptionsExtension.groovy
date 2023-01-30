package io.github.proto4j.esa.gradle

import groovy.transform.Internal
import io.github.proto4j.esa.gradle.dx.DxClassInfo

class DexOptionsExtension {

    private boolean useStrict = false

    private boolean optimize = false

    private int minimumSdkVersion = 26

    private String outputPath

    @Internal
    private Set<DxClassInfo> classes

    boolean getUseStrict() {
        return useStrict
    }

    void setUseStrict(boolean useStrict) {
        this.useStrict = useStrict
    }

    int getMinimumSdkVersion() {
        return minimumSdkVersion
    }

    void setMinimumSdkVersion(int minimumSdkVersion) {
        this.minimumSdkVersion = minimumSdkVersion
    }

    String getOutputPath() {
        return outputPath
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    boolean shouldOptimize() {
        return optimize
    }

    void setOptimize(boolean optimize) {
        this.optimize = optimize
    }

    @Internal
    Set<DxClassInfo> getClasses() {
        return classes
    }

    @Internal
    void setClasses(Set<DxClassInfo> classes) {
        this.classes = classes
    }
}