package io.github.proto4j.esa.gradle.internal

import io.github.proto4j.esa.gradle.zip.ZipCompressorFactory
import org.apache.tools.zip.Zip64Mode
import org.apache.tools.zip.ZipOutputStream

class ZipCompressorFactoryImpl implements ZipCompressorFactory {
    private final int method // default is DEFLATED
    private final Zip64Mode mode

    ZipCompressorFactoryImpl(boolean allow64Mode, int method) {
        this.method = method
        this.mode = allow64Mode ? Zip64Mode.AsNeeded : Zip64Mode.Never
    }

    @Override
    ZipOutputStream createArchiveOutputStream(File file) throws IOException {
        try {
            ZipOutputStream zos = new ZipOutputStream(file)
            zos.setUseZip64(mode)
            zos.setMethod(method)
            return zos;
        } catch (IOException e) {
            String message = String.format("Unable to create ZIP output stream for file %s.", file)
            throw new UncheckedIOException(message, e)
        }
    }
}
