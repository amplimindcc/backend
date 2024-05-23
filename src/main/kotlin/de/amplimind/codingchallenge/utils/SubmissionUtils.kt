package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import java.io.File
import java.net.URI
import java.util.Base64

/**
 * Utils object for working with submission files
 */
object SubmissionUtils {
    fun fillReadme(
        repoName: String,
        submitSolutionRequestDTO: SubmitSolutionRequestDTO,
    ): String {
        val file = File(URI(this::class.java.classLoader.getResource("ReadmeTemplate.md").toString())).readText()
        val readmeContent =
            file
                .replace("\${user}", repoName)
                .replace("\${language}", submitSolutionRequestDTO.language)
                .replace("\${version}", submitSolutionRequestDTO.version)
                .replace("\${description}", submitSolutionRequestDTO.description ?: "")
        return Base64.getEncoder().encodeToString(readmeContent.toByteArray())
    }

    fun getLintWorkflowYml(): String {
        val file = File(this::class.java.classLoader.getResource("workflows/lint.yml").file)
        val byteArray = file.readBytes() // Read file contents as byte array
        return Base64.getEncoder().encodeToString(byteArray)
    }
}
