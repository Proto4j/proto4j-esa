package io.github.proto4j.esa.gradle.dx

import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import java.nio.file.Path

/**
 * Simple entry wrapper that stores the name and absolute path of a file
 * that has been marked with {@code @Shadow}.
 *
 * @see DxAPI
 */
class DxClassInfo {

    /**
     * The absolute path of the Java class file
     */
    @PathSensitive(PathSensitivity.ABSOLUTE)
    private String path

    /**
     * The filename <b>with</b> extension (e.g. Foo.class)
     */
    private String name

    /**
     * Creates a new {@code DxClassInfo} from the given absolute file path.
     *
     * @param path the absolute path
     */
    DxClassInfo(String path) {
        this.path = path.replaceAll("\\", "/")

        String[] values = path.split("/")
        this.name = values[values.length - 1]
    }

    /**
     * Returns the absolute path of the Java class file.
     *
     * @return The absolute path of the Java class file
     */
    String getPath() {
        return path
    }

    /**
     * Returns the filename <b>with</b> extension (e.g. Foo.class)
     *
     * @return The filename with extension
     */
    String getName() {
        return name
    }

    /**
     * Returns the Java-Path instance according to the absolute file path.
     *
     * @return the absolute path as a {@code Path} object
     */
    Path getFilePath() {
        return Path.of(getPath())
    }
}
