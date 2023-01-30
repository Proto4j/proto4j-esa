package io.github.proto4j.esa.gradle.zip

import org.apache.tools.zip.ZipEntry
import org.gradle.api.Action

interface ZipWriter extends Closeable {

    void put(String path, InputStream source) throws IOException

    void put(String path, InputStream source, Action<ZipEntry> action)


}