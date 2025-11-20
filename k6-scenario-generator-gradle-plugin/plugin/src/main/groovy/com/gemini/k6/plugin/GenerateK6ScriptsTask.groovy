package com.gemini.k6.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateK6ScriptsTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getApiMetaFile()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    @TaskAction
    void generate() {
        def generator = new K6ScriptGenerator()
        generator.generate(getApiMetaFile().get().asFile, getOutputDir().get().asFile)
    }
}
