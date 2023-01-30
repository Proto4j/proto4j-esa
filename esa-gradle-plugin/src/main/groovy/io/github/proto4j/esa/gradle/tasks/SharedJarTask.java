package io.github.proto4j.esa.gradle.tasks; //@date 24.01.2023

import io.github.proto4j.esa.gradle.DexOptionsExtension;
import io.github.proto4j.esa.gradle.ESAPluginExtension;
import io.github.proto4j.esa.gradle.ESAPluginSpec;
import io.github.proto4j.esa.gradle.internal.ZipCompressorFactoryImpl;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.internal.DefaultManifest;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.internal.impldep.org.apache.tools.zip.ZipOutputStream;

import javax.annotation.Nonnull;
import java.io.File;

@CacheableTask
public class SharedJarTask extends Jar implements ESAPluginSpec {

    private FileCollection      sourceSetsClassesDirs;
    private ESAPluginExtension extension;
    private DexOptionsExtension dexOptions;

    public SharedJarTask() {
        super();
        setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
        getArchiveBaseName().set(JAR_BASE_NAME);
        setManifest(new DefaultManifest(getServices().get(FileResolver.class)));
    }

    @Override
    @Nonnull
    protected CopyAction createCopyAction() {
        DocumentationRegistry registry = getServices().get(DocumentationRegistry.class);
        return new SharedJarCopyAction(
                getArchiveFile().get().getAsFile(),
                new ZipCompressorFactoryImpl(true, ZipOutputStream.DEFLATED),
                registry, getProject().getBuildDir(), extension, dexOptions);
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    FileCollection getSourceSetsClassesDirs() {
        if (sourceSetsClassesDirs == null) {
            ConfigurableFileCollection all = getProject().getObjects().fileCollection();
            sourceSetsClassesDirs = all.filter(File::isDirectory);
        }
        return sourceSetsClassesDirs;
    }

    @TaskAction
    protected void copy() {
        super.copy();
    }

    public void setPluginExtension(ESAPluginExtension extension) {
        this.extension = extension;
        if (extension != null && extension.shouldCreateDexFile()) {
            Attributes attributes = getManifest().getAttributes();
            attributes.putIfAbsent(DX_ATTRIBUTE_KEY, DX_FILENAME);
        }
    }

    public void setDexOptions(DexOptionsExtension dexOptions) {
        this.dexOptions = dexOptions;
    }
}
