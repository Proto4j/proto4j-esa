package io.github.proto4j.esa.gradle.zip

import org.apache.tools.zip.ZipEntry
import org.gradle.api.Action

/**
 * {@code ZipWriter} objects are used to create zip entries in a
 * {@code ZipOutputStream}.
 *
 * @see ZipCompressorFactory
 */
interface ZipWriter extends Closeable {

    /**
     * Creates a new {@code ZipEntry} with and copies all data from
     * the given {@code InputStream} into it.
     *
     * @param path the relative file path
     * @param source the stream that contains all source bytes
     * @throws IOException if any I/O error occurs
     */
    void put(String path, InputStream source) throws IOException

    /**
     * Creates a new {@code ZipEntry} with and copies all data from
     * the given {@code InputStream} into it. The action will be
     * executed <b>before</b> copying all bytes.
     *
     * @param action the action to apply on the created {@code ZipEntry}
     * @param path the relative file path
     * @param source the stream that contains all source bytes
     * @throws IOException if any I/O error occurs
     */
    void put(String path, InputStream source, Action<ZipEntry> action)

}