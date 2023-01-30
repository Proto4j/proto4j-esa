package io.github.proto4j.esa.gradle.action

import org.apache.commons.io.FilenameUtils
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.CopyActionProcessingStreamAction
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal

abstract class AbstractStreamAction implements CopyActionProcessingStreamAction {

    protected static final boolean isClass(FileCopyDetails fileCopyDetails) {
        return FilenameUtils.getExtension(fileCopyDetails.path) == 'class'
    }

    @Override
    void processFile(FileCopyDetailsInternal fileCopyDetailsInternal) {
        if (fileCopyDetailsInternal.directory) {
            visitDirectory(fileCopyDetailsInternal)
        }
        else {
            visitFile(fileCopyDetailsInternal)
        }
    }

    protected abstract void visitDirectory(FileCopyDetails fileCopyDetails)
    protected abstract void visitFile(FileCopyDetails fileCopyDetails)
}
