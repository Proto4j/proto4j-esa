package io.github.proto4j.esa.gradle.action

import org.gradle.api.Action

/**
 * The strategy used to delete individual files or directories.
 *
 * @see io.github.proto4j.esa.gradle.tasks.SharedJarCopyAction
 */
abstract class DeletionStrategy implements Action<Iterable<String>> {

    /**
     * Tries to delete every item in the provided iterable.
     *
     * @param strings the items to delete
     */
    @Override
    void execute(Iterable<String> strings) {
        if (strings == null) return

        for (String path in strings) {
            if (path == null) continue

            deleteFileOrDirectory(path)
        }
    }

    /**
     * Tries to delete the file or directory.
     *
     * @param path the absolute path
     */
    public abstract void deleteFileOrDirectory(String path)
}
