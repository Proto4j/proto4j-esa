package io.github.proto4j.esa.gradle.dx

import java.nio.file.Path

class DxClassInfo {
    private String path
    private String name

    DxClassInfo(String path) {
        this.path = path

        String[] values = path.split("/")
        this.name = values[values.length - 1]
    }

    String getPath() {
        return path
    }

    void setPath(String path) {
        this.path = path
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    Path getFilePath() {
        return Path.of(getPath())
    }
}
