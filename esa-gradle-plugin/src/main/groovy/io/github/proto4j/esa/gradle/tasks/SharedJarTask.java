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

/**
 * A simple {@code Jar}-Task that configures extra manifest attributes and uses
 * a custom {@code CopyAction}.
 *
 * @see SharedJarCopyAction
 */
@CacheableTask
public class SharedJarTask extends Jar implements ESAPluginSpec {

    /**
     * The plugin configuration
     */
    private ESAPluginExtension extension;

    /**
     * optional: the DEX-file configuration
     */
    private DexOptionsExtension dexOptions;

    /**
     * Creates a new {@code Jar}-Task with {@link #JAR_BASE_NAME} as its base
     * name and a default manifest instance.
     */
    public SharedJarTask() {
        super();
        setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
        getArchiveBaseName().set(JAR_BASE_NAME);
        setManifest(new DefaultManifest(getServices().get(FileResolver.class)));
    }

    /**
     * Creates the copy action for this task.
     *
     * @return the created opy action
     * @see SharedJarCopyAction
     */
    @Override
    @Nonnull
    protected CopyAction createCopyAction() {
        DocumentationRegistry registry = getServices().get(DocumentationRegistry.class);
        return new SharedJarCopyAction(
                getArchiveFile().get().getAsFile(),
                new ZipCompressorFactoryImpl(true, ZipOutputStream.DEFLATED),
                registry, getProject().getBuildDir(), extension, dexOptions);
    }

    /**
     * Executes this task.
     */
    @TaskAction
    protected void copy() {
        super.copy();
    }

    /**
     * Applies the plugin configuration and additionally, adds the
     * {@link #DX_ATTRIBUTE_KEY} to the manifest if a DEX-file should be
     * created.
     *
     * @param extension the configuration
     */
    public void setPluginExtension(ESAPluginExtension extension) {
        this.extension = extension;
        if (extension != null && extension.shouldCreateDexFile()) {
            Attributes attributes = getManifest().getAttributes();
            attributes.putIfAbsent(DX_ATTRIBUTE_KEY, DX_FILENAME);
        }
    }

    /**
     * Applies custom DEX-file configurations.
     *
     * @param dexOptions the DEX-file configuration
     */
    public void setDexOptions(DexOptionsExtension dexOptions) {
        this.dexOptions = dexOptions;
    }
}
