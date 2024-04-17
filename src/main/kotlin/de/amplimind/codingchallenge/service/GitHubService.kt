package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.SubmissionUtils
import jakarta.validation.Payload
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.util.*

@Service
class GitHubService (
    private val submissionRepository: SubmissionRepository,
) {

    val accessToken = "GitHub Access Token"
    /**
     * Upload the code to the Repository.
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     */
    // TODO: optimize requests
    suspend fun pushToRepo(submitSolutionRequestDTO: SubmitSolutionRequestDTO, userEmail: String) = coroutineScope {
        val owner = userEmail.split("@")[0]
        if(!submissionGitRepositoryExists(userEmail)) {
            launch {
                val repoUrl = "orgs/amplimindcc/repos"
                val jsonPayload = "{\"name\": \"${owner}\",\"description\": \"This is the submission repository of ${owner}\"}"
                createHttpRequest(repoUrl, "POST", jsonPayload);
            }.join()
        }
        //runBlocking(context = EmptyCoroutineContext) {
        val deferredFiles: List<Deferred<Result<Int>>> = SubmissionUtils.unzipCode(submitSolutionRequestDTO.zipFileContent).map { entry ->
            async {
                // val filepathWithoutFilename = entry.key.substring(entry.key.indexOf("/", 0), entry.key.lastIndexOf("/")).ifEmpty { "/" }
                val filePath = entry.key.substringAfter("/")
                val fileContent = entry.value
                val codeUrl = "repos/amplimindcc/${owner}/contents/${filePath}"
                val jsonPayload = "{\"content\": \"${fileContent}\"}"
                createHttpRequest(codeUrl, "POST", jsonPayload)

            }
        }
        val deferredReadMe = async {
            val readmePath = "README.md"
            val readmeContent = Base64.getEncoder().encodeToString(submitSolutionRequestDTO.description.toByteArray())
            val codeUrl = "repos/amplimindcc/${owner}/contents/${readmePath}"
            val jsonPayload = "{\"content\": \"${readmeContent}\"}"
            createHttpRequest(codeUrl, "POST", jsonPayload)
        }
        val deferredWorkflow = async {
            val workflowPath = ".github/workflows/lint.yml"
            val classLoader = this::class.java.classLoader
            val file = File(classLoader.getResource("resources/workflows/lint.yml")?.file)
            val byteArray = file.readBytes() // Read file contents as byte array
            val workflowContent = Base64.getEncoder().encodeToString(byteArray)
            val workFlowUrl = "repos/amplimindcc/${owner}/contents/${workflowPath}"
            val jsonPayload = "{\"content\": \"${workflowContent}\"}"
            createHttpRequest(workFlowUrl, "POST", jsonPayload)
        }
        val statusCodes = awaitAll(*deferredFiles.toTypedArray(), deferredWorkflow, deferredReadMe)
        val deferredLinting = async {
            val workflowName = "lint"
            val lintUrl = "repos/amplimindcc/${owner}/actions/workflows/${workflowName}/dispatches"
            val jsonPayload = "{\"ref\": \"main\"}"
            createHttpRequest(lintUrl, "POST", jsonPayload)
        }
    }

    /**
     * Checks if the there already exists a GitHub Repository for the user.
     * @param userEmail the email of the user who made the submission
     * @return the [Boolean] if it exists or not
     */
    fun submissionGitRepositoryExists(userEmail: String): Boolean {
        val submission = this.submissionRepository.findByUserEmail(userEmail)
            ?:throw ResourceNotFoundException("Submission with email ${userEmail} was not found");
        return submission.status.matchesAny(SubmissionStates.SUBMITTED);
    }

    /**
     * creates a https request to the GitHub api.
     * @param url the endpoint of the GitHub api
     * @param requestMethod the http request method to be used
     * @param jsonPayload the payload that should be sent in the request body
     * @return the [Result] of the http request
     */
    suspend fun createHttpRequest(url: String, requestMethod: String, jsonPayload: String): Result<Int> {
        return try {
            val url = URI("https://api.github.com/${url}").toURL()
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = requestMethod
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
            }
            val statusCode = connection.responseCode
            connection.disconnect()
            Result.success(statusCode)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }


}