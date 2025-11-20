package com.gemini.k6.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class K6ScenarioGeneratorPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.getPluginManager().apply("java")

        project.tasks.register("generateK6Scripts", JavaExec) {
            group = "k6"
            description = "Generates k6 JavaScript scripts from Spring Boot controllers."
            classpath = project.sourceSets.main.runtimeClasspath
            mainClass = "com.gemini.k6.scriptgenerator.K6ScriptGenerator"
            // TODO: set args
        }
    }
}
