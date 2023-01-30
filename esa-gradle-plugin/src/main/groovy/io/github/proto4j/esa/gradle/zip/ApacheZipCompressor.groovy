package io.github.proto4j.esa.gradle.zip

import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.internal.file.archive.compression.ArchiveOutputStreamFactory

interface ApacheZipCompressor extends ArchiveOutputStreamFactory{

    ZipOutputStream createArchiveOutputStream(File file) throws IOException
}