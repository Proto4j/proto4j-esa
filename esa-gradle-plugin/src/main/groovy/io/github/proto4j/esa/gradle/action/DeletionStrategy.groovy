package io.github.proto4j.esa.gradle.action

import org.gradle.api.Action

abstract class DeletionStrategy implements Action<Iterable<String>> {

    @Override
    void execute(Iterable<String> strings) {
        if (strings == null) return

        for (String path in strings) {
            if (path == null) continue

            deleteFileOrDirectory(path)
        }
    }

    public abstract void deleteFileOrDirectory(String path)
}
