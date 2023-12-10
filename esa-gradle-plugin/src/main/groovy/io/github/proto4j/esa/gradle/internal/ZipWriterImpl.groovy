package io.github.proto4j.esa.gradle.internal

import io.github.proto4j.esa.gradle.zip.ZipWriter
import org.apache.commons.io.IOUtils
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.Action

class ZipWriterImpl implements ZipWriter {
    private ZipOutputStream zos

    ZipWriterImpl(ZipOutputStream zipOutputStream) {
        this.zos = zipOutputStream
    }

    @Override
    void put(String path, InputStream source) throws IOException {
        put(path, source, null)
    }

    @Override
    void put(String path, InputStream source, Action<ZipEntry> action) {
        ZipEntry zipEntry = new ZipEntry(path)
        if (action != null) {
            action.execute(zipEntry)
        }
        zos.putNextEntry(zipEntry)
        IOUtils.copyLarge(source, zos)
        zos.closeEntry()
    }

    @Override
    void close() throws IOException {
        zos.close()
    }
}
