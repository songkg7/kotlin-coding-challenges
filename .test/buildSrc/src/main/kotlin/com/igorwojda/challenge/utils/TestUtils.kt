package com.igorwojda.challenge.utils

import org.gradle.api.Project
import java.io.File

object TestUtils {

    fun generateTestFiles(project: Project) {
        getPuzzleDirectories(project).forEach {
            generateTestFiles(it)
        }
    }

    /**
     * Generate test files for a given puzzle by combining challenge file with available solutions
     */
    private fun generateTestFiles(puzzleDirectoryPath: File) {
        val generatedPuzzleDirecotryPath = puzzleDirectoryPath
            .path
            .replace("kotlin/com/igorwojda/", "kotlin/generated/com/igorwojda/")

        // May be already pre-cached by CI
        deleteDirectory(File(generatedPuzzleDirecotryPath))

        val testFiles = KotlinGeneratorUtils.getTestFiles(puzzleDirectoryPath)

        testFiles
            .forEach {
                createTestFile(generatedPuzzleDirecotryPath, it)
            }
    }

    private fun deleteDirectory(directory: File) {
        if (directory.isDirectory) {
            val files = directory.listFiles()

            // if the directory contains any file
            if (files != null) {
                for (file in files) {

                    // recursive call if the subdirectory is non-empty
                    deleteDirectory(file)
                }
            }
        }
        directory.delete()
    }

    /**
     * Create a test files for a given puzzle
     */
    private fun createTestFile(generatedPuzzleDirecotryPath: String, testFile: TestFile) {
        val testDirectory = File("$generatedPuzzleDirecotryPath/${testFile.relativePath}/")
        testDirectory.mkdirs()

        val targetChallengeFile = File("${testDirectory.path}/${testFile.fileName}")
        targetChallengeFile.writeText(testFile.lines.joinToString(separator = "\n"))
    }

    /**
     * Return list of project names
     */
    private fun getPuzzleDirectories(project: Project): List<File> {
        val path = "${project.rootDir.path}/../src/test/kotlin/com/igorwojda"
        val directory = File(path)
        val miscDirectoryName = "misc"

        return directory
            .walk()
            .filter { it.isDirectory }
            .filter { it.name != miscDirectoryName }
            .filter { it.isHighLevelDirectory }
            .toList()
    }

    /**
     * Checks whatever or not directory is high level directory (puzzle grouping directory)
     */
    private val File.isHighLevelDirectory get() = this.isDirectory && this.listFiles().none { it.isDirectory }
}
