package io.github.proto4j.esa.gradle.tasks

import io.github.proto4j.esa.api.ISharedClassInfo
import io.github.proto4j.esa.gradle.APIUtil
import io.github.proto4j.esa.gradle.DexOptionsExtension
import io.github.proto4j.esa.gradle.ESAPluginExtension
import io.github.proto4j.esa.gradle.ESAPluginSpec
import io.github.proto4j.esa.gradle.action.AbstractStreamAction
import io.github.proto4j.esa.gradle.action.DeletionStrategy
import io.github.proto4j.esa.gradle.dx.DxAPI
import io.github.proto4j.esa.gradle.dx.DxClassInfo
import io.github.proto4j.esa.gradle.internal.ZipWriterImpl
import io.github.proto4j.esa.gradle.zip.ZipCompressorFactory
import io.github.proto4j.esa.gradle.zip.ZipWriter
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.tools.zip.Zip64RequiredException
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.UncheckedException
import org.objectweb.asm.Type

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

//@date 25.01.2023

import java.nio.file.Files
import java.nio.file.Path

/**
 * A customized copy action that inspects all source-code files and copies classes
 * marked with {@code @Shadow} to the shared JAR file.
 * <p>
 * This action removes all class file copied into the generated JAR afterwards. In
 * addition to that, classes that store {@code @Encrypt} will be transformed as well,
 * so their fields are encrypted.
 * <p>
 * Exceptions are usually rethrown to enable issue tracking and warnings will be
 * printed with {@code System.err}.
 */
class SharedJarCopyAction implements CopyAction, ESAPluginSpec {

    /**
     * Used to print information about the ZipCompressor library
     */
    private final DocumentationRegistry registry

    /**
     * The factory that creates the shared JAR output stream
     */
    private final ZipCompressorFactory compressor

    /**
     * The key used for encryption
     */
    private final SecretKey encryptionKey

    /**
     * The DEX-file configuration
     */
    private final DexOptionsExtension dexOptions

    /**
     * The plugin configuration
     */
    private final ESAPluginExtension extension

    /**
     * The action/ strategy that deletes any input elements.
     *
     * @see InternalDeleteAction
     */
    private final DeletionStrategy deletionStrategy

    /**
     * The temporary ZIP-File stored in the libs/ directory.
     */
    private final File zipFile

    /**
     * Orientation file object pointing at the build-directory of this project.
     */
    private final File buildDir

    /**
     * Additional helper to create zip entries in the shared JAR file.
     */
    private ZipWriter zipWriter

    /**
     * As there can be only one destination class, it is stored in a single
     * {@code Type} variable.
     */
    private Type outputClass

    /**
     * A collection of relative path values.
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    private Set<String> shadowedClasses

    SharedJarCopyAction(
            File zipFile, ZipCompressorFactory compressor, DocumentationRegistry registry,
            File buildDir, ESAPluginExtension extension, DexOptionsExtension dexOptions
    ) {
        this.dexOptions = dexOptions
        this.extension = extension
        this.zipFile = zipFile
        this.compressor = compressor
        this.registry = registry
        this.buildDir = buildDir
        this.encryptionKey = new SecretKeySpec(extension.getKey().getBytes(), "AES")
        this.deletionStrategy = new InternalDeleteAction()

    }

    /**
     * Creates an embedded shared archive (ESA)
     *
     * @param stream the processing stream containing all resources
     * @return the result of this action
     */
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
                        String.format("%s\n\nTo build an ESA file, please enable the zip64 extension.\nSee: %s",
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
            System.err.println("ERROR - Could not encrypt JAR file! (maybe wrong key or null-key")
            return WorkResults.didWork(false)
        }

        String outputPath
        String name
        if (outputClass == null) {
            System.err.println("WARNING: No @Output class specified (defaulting to defpackage/JarContent)")
            //TODO: extract configurable variables
            outputClass = Type.getType("Ldefpackage/JarContent;")
            outputPath = buildDir.absolutePath + "/classes/java/main/defpackage/"
            name = "JarContent.class"
        } else {
            outputPath = buildDir.absolutePath + "/classes/java/main/"
            name = outputClass.getInternalName() + ".class"
        }

        try {
            APIUtil.writeOutputClass(outputPath, name, extension.esaFilename, encryptedJar, outputClass)
        } catch (Throwable e) {
            throw UncheckedException.throwAsUncheckedException(e)
        }

        return WorkResults.didWork(true)
    }

    /**
     * Executes the action on the given resource and closes the resource
     * afterwards.
     *
     * @param resource the resource to be worked with
     * @param action the action to be executed
     */
    static <T extends Closeable> void with(T resource, Action<T> action) {
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

    /**
     * Creates the DEX-file that stores all shared classes. This action is executed
     * before removing all class files marked with {@code @Shadow}.
     */
    void createDexFile() {
        Set<DxClassInfo> set = new HashSet<>()
        shadowedClasses.forEach { path ->
            String basePath = buildDir.absolutePath + '/classes/'
            set.add(new DxClassInfo(basePath + path))
        }

        dexOptions.setClasses(set)
        byte[] rawFile = DxAPI.toDex(dexOptions)
        zipWriter.put(DX_FILENAME, new ByteArrayInputStream(rawFile))
    }

    /**
     * The internal deletion strategy: deletes a file or directory if present
     */
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
            //nop
        }

        @Override
        protected void visitFile(FileCopyDetails fileCopyDetails) {
            if (isClass(fileCopyDetails)) {
                remap(fileCopyDetails)
            }
        }

        private void remap(FileCopyDetails fileCopyDetails) {
            if (isClass(fileCopyDetails)) {
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
                        if (outputClass != null) {
                            System.err.println("WARNING: Ignoring second @Output class > " + classInfo.getName())
                            return
                        }
                        outputClass = classInfo.getType()

                        if (classInfo.isShadowed()) {
                            System.err.println("WARNING: @Output class is marked as @Shadow - will be ignored > "
                                    + classInfo.getName())
                        }
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
