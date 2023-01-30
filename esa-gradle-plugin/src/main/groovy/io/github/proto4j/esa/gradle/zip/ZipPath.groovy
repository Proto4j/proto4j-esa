package io.github.proto4j.esa.gradle.zip

import org.gradle.api.file.RelativePath
import org.gradle.internal.impldep.org.apache.tools.zip.ZipEntry

final class ZipPath extends RelativePath {

    ZipEntry zipEntry

    ZipPath(ZipEntry zipEntry) {
        super(!zipEntry.directory, zipEntry.name.split("/"))
        this.zipEntry = zipEntry
    }

    boolean isClass() {
        return lastName.endsWith('.class')
    }

    @Override
    ZipPath getParent() {
        if (!segments || segments.length == 0) {
            return null
        }
        else {
            String path = segments[0..-2].join('/') + '/'
            return new ZipPath(new ZipEntry(path))
        }
    }
}
