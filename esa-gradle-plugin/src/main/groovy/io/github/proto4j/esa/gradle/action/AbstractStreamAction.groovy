package io.github.proto4j.esa.gradle.action

import org.apache.commons.io.FilenameUtils
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.CopyActionProcessingStreamAction
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal

/**
 * Base class to copy the marked Java class files.
 *
 * @see io.github.proto4j.esa.gradle.tasks.SharedJarCopyAction
 */
abstract class AbstractStreamAction implements CopyActionProcessingStreamAction {

    /**
     * Returns whether the given file details are linked to an existing Java
     * class file.
     *
     * @param fileCopyDetails the file details
     * @return whether the file is a Java class file
     */
    protected static final boolean isClass(FileCopyDetails fileCopyDetails) {
        return FilenameUtils.getExtension(fileCopyDetails.path) == 'class'
    }

    /**
     * Processes an individual file and delegates its further processing either to
     * {@link #visitDirectory(org.gradle.api.file.FileCopyDetails)} or to
     * {@link #visitFile(org.gradle.api.file.FileCopyDetails)}.
     *
     * @param fileCopyDetailsInternal the file details
     */
    @Override
    void processFile(FileCopyDetailsInternal fileCopyDetailsInternal) {
        if (fileCopyDetailsInternal.directory) {
            visitDirectory(fileCopyDetailsInternal)
        }
        else {
            visitFile(fileCopyDetailsInternal)
        }
    }

    /**
     * Processes an individual directory.
     *
     * @param fileCopyDetails the directory details
     */
    protected abstract void visitDirectory(FileCopyDetails fileCopyDetails)

    /**
     * Processes an individual file. This method should only process Java class
     * files (check could be done with {@link #isClass(org.gradle.api.file.FileCopyDetails)}.
     *
     * @param fileCopyDetails the file details
     */
    protected abstract void visitFile(FileCopyDetails fileCopyDetails)
}
