package io.github.proto4j.esa.gradle

import io.github.proto4j.esa.gradle.tasks.SharedJarTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

class ESAPlugin implements Plugin<Project>, ESAPluginSpec {

    @Override
    void apply(Project project) {
        if (GradleVersion.current() < GradleVersion.version("7.0")) {
            throw new GradleException("This version of Crypto-Gradle supports Gradle 7.0+ only.")
        }

        ESAPluginExtension extension = project.extensions.create(EXTENSION_NAME, ESAPluginExtension, project)
        project.configurations.create(CONFIG_NAME)

        DexOptionsExtension dexOptions = project.extensions.create(DX_EXTENSION_NAME, DexOptionsExtension)

        configureTask(extension, dexOptions)
//        project.configurations.compileClasspath.extendsFrom project.configurations.aesjar


        project.tasks.named('classes') {
            it.dependsOn(project.tasks.named(JAR_TASK_NAME))
        }
        project.tasks.named(JAR_TASK_NAME) {
            it.dependsOn(project.tasks.named('compileJava'))
        }
    }

    protected static void configureTask(ESAPluginExtension extension, DexOptionsExtension dexOptions) {
        Project project = extension.getProject()

        project.tasks.register(JAR_TASK_NAME, SharedJarTask) { aesjar ->
            aesjar.group = GROUP_NAME
            aesjar.description = 'Template'
            aesjar.setExtension(extension)
            aesjar.setDexOptions(dexOptions)

            outputs.upToDateWhen { false }

            aesjar.archiveClassifier.set("all")
            def files = project.objects.fileCollection().from { ->
                project.configurations.named(CONFIG_NAME)
            }

            File classes = new File(project.getBuildDir(), "/classes/")
            aesjar.from(classes)
        }
        project.artifacts.add(CONFIG_NAME, project.tasks.named(JAR_TASK_NAME))
    }
}
