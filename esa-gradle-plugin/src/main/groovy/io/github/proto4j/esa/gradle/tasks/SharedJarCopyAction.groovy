package io.github.proto4j.esa.gradle.tasks

import io.github.proto4j.esa.api.ISharedClassInfo
import io.github.proto4j.esa.gradle.DexOptionsExtension
import io.github.proto4j.esa.gradle.ESAPluginExtension
import io.github.proto4j.esa.gradle.ESAPluginSpec
import io.github.proto4j.esa.gradle.action.AbstractStreamAction
import io.github.proto4j.esa.gradle.action.DeletionStrategy
import io.github.proto4j.esa.gradle.dx.DxAPI
import io.github.proto4j.esa.gradle.dx.DxClassInfo
import io.github.proto4j.esa.gradle.APIUtil
import io.github.proto4j.esa.gradle.internal.ZipWriterImpl
import io.github.proto4j.esa.gradle.zip.ApacheZipCompressor
import io.github.proto4j.esa.gradle.zip.ZipWriter
import org.apache.commons.io.FilenameUtils
import org.apache.tools.zip.Zip64RequiredException
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.UncheckedException
import org.objectweb.asm.Type

import javax.crypto.SecretKey

//@date 25.01.2023

import javax.crypto.spec.SecretKeySpec
import java.nio.file.Files
import java.nio.file.Path

class SharedJarCopyAction implements CopyAction, ESAPluginSpec {

    private final DocumentationRegistry registry
    private final ApacheZipCompressor compressor

    private final SecretKey encryptionKey
    private final DexOptionsExtension dexOptions
    private final ESAPluginExtension extension

    private final DeletionStrategy deletionStrategy

    private final File zipFile
    private final File buildDir
    private final File tmpDir

    private ZipWriter zipWriter
    private Type outputClass
    private Set<String> shadowedClasses

    public SharedJarCopyAction(
            File zipFile, ApacheZipCompressor compressor, DocumentationRegistry registry,
            File buildDir, ESAPluginExtension extension, DexOptionsExtension dexOptions
    ) {
        this.dexOptions = dexOptions
        this.extension = extension
        this.zipFile = zipFile
        this.compressor = compressor
        this.registry = registry
        this.buildDir = buildDir
        this.encryptionKey = new SecretKeySpec(extension.getKey().getBytes(), "AES")
        this.tmpDir = new File(buildDir, TEMP_DIR_NAME)
        this.deletionStrategy = new InternalDeleteAction()

        if (!tmpDir.exists()) {
            tmpDir.createNewFile()
        }
    }

    @Override
    WorkResult execute(CopyActionProcessingStream stream) {
        ZipOutputStream zipOutputStream
        try {
            zipOutputStream = compressor.createArchiveOutputStream(zipFile)
        } catch (Exception e) {
            throw new GradleException("", e)
        }

        try {
            with(zipOutputStream, new Action<ZipOutputStream>() {
                @Override
                void execute(ZipOutputStream zos) {
                    zipWriter = new ZipWriterImpl(zos)
                    stream.process(new InternalStreamAction(zos))
                    // Before removing all shadowed classes, create the DexFile (if enabled)
                    if (extension.shouldCreateDexFile()) {
                        createDexFile()
                    }
                }
            })
        } catch (UncheckedIOException e) {
            if (e.cause instanceof Zip64RequiredException) {
                throw new Zip64RequiredException(
                        String.format("%s\n\nTo build this archive, please enable the zip64 extension.\nSee: %s",
                                e.cause.message, registry.getDslRefForProperty(Zip, "zip64"))
                )
            }
        }

        deletionStrategy.execute(shadowedClasses)

        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        try (InputStream is = zipFile.newInputStream()) {
            bos.write(is.readAllBytes())
        }

        byte[] jarContent = bos.toByteArray()
        byte[] encryptedJar = APIUtil.encryptJar(jarContent, encryptionKey)

        bos.close()
        if (encryptedJar.length == 0) {
            System.err.println("ERROR - Could not encrypt JAR file!")
            return WorkResults.didWork(false)
        }

        String outputPath
        String name
        if (outputClass == null) {
            //TODO: extract configurable variables
            outputClass = Type.getType("Ldefpackage/JarContent;")
            outputPath = buildDir.absolutePath + "/classes/java/main/defpackage/"
            name = "JarContent.class"
        } else {
            outputPath = buildDir.absolutePath + "/classes/java/main/"
            name = outputClass.getInternalName() + ".class"
        }

        try {
            APIUtil.writeOutputClass(outputPath, name, "name", encryptedJar, outputClass)
        } catch (Throwable e) {
            throw UncheckedException.throwAsUncheckedException(e)
        }

        return WorkResults.didWork(true)
    }

    public static <T extends Closeable> void with(T resource, Action<T> action) {
        try {
            action.execute(resource)
        } catch (Throwable t) {
            try {
                resource.close()
            } catch (IOException ignored) {
            }
            throw UncheckedException.throwAsUncheckedException(t)
        } finally {
            try {
                resource.close()
            } catch (IOException e) {
                throw new UncheckedIOException(e)
            }
        }
    }

    void createDexFile() {
        Set<DxClassInfo> set = new HashSet<>()
        shadowedClasses.forEach {path ->
            String basePath = buildDir.absolutePath + '/classes/'
            set.add(new DxClassInfo(basePath + path))
        }

        dexOptions.setClasses(set)
        byte[] rawFile = DxAPI.toDex(dexOptions)
        zipWriter.put(DX_FILENAME, new ByteArrayInputStream(rawFile))
    }

    private class InternalDeleteAction extends DeletionStrategy {

        @Override
        void deleteFileOrDirectory(String path) {
            if (!Files.deleteIfExists(Path.of(path))) {
                Files.deleteIfExists(Path.of(buildDir.absolutePath + "/classes/" + path))
            }
        }
    }

    private class InternalStreamAction extends AbstractStreamAction {

        private final ZipOutputStream zos

        InternalStreamAction(ZipOutputStream zos) {
            this.zos = zos
            shadowedClasses = new HashSet<>()
        }

        @Override
        protected void visitDirectory(FileCopyDetails fileCopyDetails) {

        }

        @Override
        protected void visitFile(FileCopyDetails fileCopyDetails) {
            if (isClass(fileCopyDetails)) {
                println fileCopyDetails.name + ": " + fileCopyDetails.relativePath + ", " + fileCopyDetails.relativeSourcePath
                remap(fileCopyDetails)
            }
        }

        private void remap(FileCopyDetails fileCopyDetails) {
            if (FilenameUtils.getExtension(fileCopyDetails.path) == 'class') {
                try (InputStream is = fileCopyDetails.file.newInputStream()) {

                    ISharedClassInfo classInfo = APIUtil.inspect(is)
                    is.close()

                    if (classInfo.hasEncryptedFields()) {
                        // encrypt all fields first
                        APIUtil.encryptAll(classInfo, encryptionKey)

                        ByteArrayOutputStream bos = new ByteArrayOutputStream()
                        try (InputStream ris = fileCopyDetails.file.newInputStream()) {
                            APIUtil.writeClass(classInfo, ris, bos)
                        }

                        try (OutputStream os = fileCopyDetails.file.newOutputStream()) {
                            os.write(bos.toByteArray())
                        }
                    }

                    if (classInfo.isOutputClass()) {
                        outputClass = classInfo.getType()
                        return
                    }

                    if (!classInfo.isShadowed()) {
                        return
                    }

                    shadowedClasses.add(fileCopyDetails.path)

                    String path = fileCopyDetails.relativeSourcePath.replaceAll("java/main/", "")
                    try (InputStream nis = fileCopyDetails.file.newInputStream()) {
                        zipWriter.put(path, nis, e -> e.setTime(fileCopyDetails.file.lastModified()))
                    }
                }
            }
        }

    }
}
