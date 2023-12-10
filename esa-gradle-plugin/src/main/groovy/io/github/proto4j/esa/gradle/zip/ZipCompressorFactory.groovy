package io.github.proto4j.esa.gradle.zip

import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.internal.file.archive.compression.ArchiveOutputStreamFactory

/**
 * Wrapper interface used to indicate we want to have the Apache
 * {@code ZipOutputStream} instead of the original Java output stream.
 *
 * @see io.github.proto4j.esa.gradle.internal.ZipCompressorFactoryImpl
 */
interface ZipCompressorFactory extends ArchiveOutputStreamFactory {

    /**
     * Creates a new {@code ZipOutputStream}.
     *
     * @param file the destination file
     * @return the newly created stream
     * @throws IOException if the stream could not be created
     */
    ZipOutputStream createArchiveOutputStream(File file) throws IOException
}