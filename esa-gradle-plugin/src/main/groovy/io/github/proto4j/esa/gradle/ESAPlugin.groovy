package io.github.proto4j.esa.gradle

import io.github.proto4j.esa.gradle.tasks.SharedJarTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

/**
 * Main class to apply the ESA-gradle-plugin to the current project.
 * <p>
 * Note that this plugin affects the build process of the provided project
 * as it intercepts it and generates the ESA file before the process can
 * resume.
 *
 * @see io.github.proto4j.esa.gradle.tasks.SharedJarCopyAction
 */
final class ESAPlugin implements Plugin<Project>, ESAPluginSpec {

    /**
     * Applies a custom configuration to the provided {@code Project}.
     * <p>
     * This action will register a new Task-Group together with a task named 'esa',
     * that is responsible for generating the ESA file. In addition to that, the
     * task named 'classes' depends on the newly registered task to prevent situations
     * where the ESA task may be ignored.
     * <p>
     * The installed task will always run after the 'compileJava' task to apply all
     * changes made to the class files directly.
     *
     * @param project the project to configure
     */
    @Override
    void apply(Project project) {
        if (GradleVersion.current() < GradleVersion.version("7.0")) {
            throw new GradleException("This version of Crypto-Gradle supports Gradle 7.0+ only.")
        }
        ESAPluginExtension extension = project.extensions
                .create(EXTENSION_NAME, ESAPluginExtension, project)

        project.configurations.create(CONFIG_NAME)

        DexOptionsExtension dexOptions = project.extensions
                .create(DX_EXTENSION_NAME, DexOptionsExtension)

        configureTask(extension, dexOptions)
        project.artifacts.add(CONFIG_NAME, project.tasks.named(JAR_TASK_NAME))

        // Execution flow:
        // 'compileJava' --> 'esa' --> 'classes'
        project.tasks.named('classes') {
            it.dependsOn(project.tasks.named(JAR_TASK_NAME))
        }
        project.tasks.named(JAR_TASK_NAME) {
            it.dependsOn(project.tasks.named('compileJava'))
        }
    }

    /**
     * Configures the 'esa'-Task.
     *
     * @param extension the default project options
     * @param dexOptions the DEX-file configuration
     */
    protected static void configureTask(ESAPluginExtension extension, DexOptionsExtension dexOptions) {
        Project project = extension.getProject()

        project.tasks.register(JAR_TASK_NAME, SharedJarTask) { esaTask ->
            esaTask.group = GROUP_NAME
            esaTask.description = 'Embedded Shared Archive (ESA) generator task'
            esaTask.setPluginExtension(extension)
            esaTask.setDexOptions(dexOptions)

            // This small line of code prevents this task to be UP-TO-DATE,
            // the ESA file has to be generated ALWAYS.
            outputs.upToDateWhen { false }

            esaTask.archiveClassifier.set("all")
            File classes = new File(project.getBuildDir(), "/classes/")
            if (!classes.exists()) {
                throw new FileNotFoundException("Make sure the \$BUILD_DIR/classes directory exists")
            }
            esaTask.from(classes)
        }
    }
}
