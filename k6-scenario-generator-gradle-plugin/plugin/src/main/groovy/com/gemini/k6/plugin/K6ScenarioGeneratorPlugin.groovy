package com.gemini.k6.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class K6ScenarioGeneratorPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.getPluginManager().withPlugin("java") {
            def generateK6ScriptsTask = project.tasks.register("generateK6Scripts", GenerateK6ScriptsTask) {
                group = "k6"
                description = "Generates k6 JavaScript scripts from Spring Boot controllers."

                def compileJavaTask = project.tasks.named("compileJava", JavaCompile.class).get()
                apiMetaFile.set(project.file(new File(compileJavaTask.getOptions().getAnnotationProcessorGeneratedSourcesDirectory().get(), "api-meta.json")))
                outputDir.set(project.layout.buildDirectory.dir("k6-scripts"))
                dependsOn(compileJavaTask)
            }
        }
    }
}
