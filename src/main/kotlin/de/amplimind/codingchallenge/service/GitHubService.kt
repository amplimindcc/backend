package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.SubmissionUtils
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
                createSubmissionRepository(owner);
            }.join()
        }
        //runBlocking(context = EmptyCoroutineContext) {
        val deferredFiles: List<Deferred<Result<Int>>> = SubmissionUtils.unzipCode(submitSolutionRequestDTO.zipFileContent).map { entry ->
            async {
                // val filepathWithoutFilename = entry.key.substring(entry.key.indexOf("/", 0), entry.key.lastIndexOf("/")).ifEmpty { "/" }
                val filePath = entry.key.substringAfter("/")
                val fileContent = entry.value
                makePutRequest(owner, filePath, fileContent)

            }
        }
        val deferredReadMe = async {
            val readmePath = "README.md"
            val readmeContent = Base64.getEncoder().encodeToString(submitSolutionRequestDTO.description.toByteArray())
            makePutRequest(owner, readmePath, readmeContent)
        }
        val deferredWorkflow = async {
            val workflowPath = ".github/workflows/lint.yml"
            val classLoader = this::class.java.classLoader
            val file = File(classLoader.getResource("resources/workflows/lint.yml")?.file)
            val byteArray = file.readBytes() // Read file contents as byte array
            val workflowContent = Base64.getEncoder().encodeToString(byteArray)
            makePutRequest(owner, workflowPath, workflowContent)
        }
        val statusCodes = awaitAll(*deferredFiles.toTypedArray(), deferredWorkflow, deferredReadMe)
        val deferredLinting = async {
            triggerLintingWorkflow(owner, "lint")
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
     * Upload the code to the Repository.
     * @param owner the owner of the repository
     * @param filePath the filepath where the file should reside in the repository
     * @param fileContent the content of the file that should be pushed
     * @param accessToken the personal access token used for authentication
     * @return the [Result] of the PUT request
     */
    suspend fun makePutRequest(owner: String, filePath: String, fileContent: String): Result<Int> {
        return try {
            val url = URI("https://api.github.com/repos/amplimindcc/${owner}/contents/${filePath}").toURL()
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonPayload = "{\"content\": \"${fileContent}\"}"
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


    /**
     * Adds a new Submission Repository.
     * @param repoName the username of the user who made the submission
     * @param accessToken the PAT token used for authentication with GitHub
     * @return the [Result] of the POST request
     */
    suspend fun createSubmissionRepository(repoName: String): Result<Int> {
        return try {
            val url = URI("https://api.github.com/orgs/amplimindcc/repos").toURL()
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonPayload = "{\"name\": \"${repoName}\",\"description\": \"This is the submission repository of ${repoName}\"}"
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

    /**
     * Adds a new Submission Repository.
     * @param owner the username of the user who made the submission
     * @param workflowName the name of the workflow that should be triggered
     * @param accessToken the PAT token used for authentication with GitHub
     * @return the [Result] of the POST request
     */
    suspend fun triggerLintingWorkflow(owner: String, workflowName: String): Result<Int> {
        return try {
            val url = URI("https://api.github.com/repos/amplimindcc/${owner}/actions/workflows/${workflowName}/dispatches").toURL()
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonPayload = "{\"ref\": \"main\"}"
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