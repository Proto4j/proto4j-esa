package io.github.proto4j.esa.gradle

import org.gradle.api.Project

class ESAPluginExtension {

    Project project

    String key

    boolean createDexFile = false

    String esaFilename = "name"

    ESAPluginExtension(Project project) {
        this.project = project
    }

    Project getProject() {
        return project
    }

    void setProject(Project project) {
        this.project = project
    }

    String getKey() {
        return key
    }

    void setKey(String key) {
        this.key = key
    }

    boolean shouldCreateDexFile() {
        return createDexFile
    }

    void setCreateDexFile(boolean createDexFile) {
        this.createDexFile = createDexFile
    }
}
