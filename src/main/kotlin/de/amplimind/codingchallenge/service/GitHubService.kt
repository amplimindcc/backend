package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.LintResultDTO
import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.TriggerWorkflowException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.*
import de.amplimind.codingchallenge.utils.ZipUtils
import kotlinx.coroutines.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.math.log

@Service
class GitHubService (
    private val submissionRepository: SubmissionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Upload the code to the Repository.
     * @param apiClient the client for GitHub api alls
     * @param submitSolutionRequestDTO the request to upload the code
     * @param repoName the email of the user who made the submission
     */
    // TODO: optimize requests
    suspend fun pushToRepo(apiClient: GitHubApiClient, submitSolutionRequestDTO: SubmitSolutionRequestDTO, repoName: String) = coroutineScope {
//        val reqCode = async { pushCode(apiClient, submitSolutionRequestDTO.zipFileContent, repoName) }
//        val reqWorkflow= async { pushWorkflow(apiClient, repoName) }
//        val reqReadme= async { pushReadme(apiClient, submitSolutionRequestDTO.description, repoName) }
//        return@coroutineScope awaitAll(reqCode, reqWorkflow, reqReadme)
        pushCode(apiClient, submitSolutionRequestDTO.zipFileContent, repoName)
        pushReadme(apiClient, submitSolutionRequestDTO.description, repoName)
        pushWorkflow(apiClient, repoName)
    }

    /**
     * Create the GitHub submission repository.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    suspend fun createRepo(apiClient: GitHubApiClient, repoName: String): Deferred<ResponseBody> = coroutineScope {
        val organisation = "amplimindcc"
        val description = "This is the submission repository of $repoName"
        val submissionRepository = SubmissionGitHubRepository(repoName, description)
        val req: Deferred<ResponseBody> = async { apiClient.createSubmissionRepository(organisation, submissionRepository) }
        req.await()
        return@coroutineScope req
    }

    /**
     * Push the code to the GitHub repository.
     * @param apiClient the client for GitHub api alls
     * @param multipartFile the code to push
     * @param repoName the owner and name of the repo
     */
    suspend fun pushCode(apiClient: GitHubApiClient, multipartFile: MultipartFile, repoName: String): Deferred<List<ResponseBody>> = coroutineScope {
        val req = async {
            ZipUtils.unzipCode(multipartFile).map { entry ->
                val filePath = entry.key.substringAfter("/")
                val fileContent = entry.value
                val submissionFileCode = SubmissionFile("committed by kotlin backend", fileContent)
                apiClient.pushFileCall(repoName, filePath, submissionFileCode)
            }
        }
        req.await()
        return@coroutineScope req
    }

    /**
     * Push the linting workflow to the GitHub repository.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    suspend fun pushWorkflow(apiClient: GitHubApiClient, repoName: String): Deferred<ResponseBody> = coroutineScope {
        val workflowPath = ".github/workflows/lint.yml"
        val file = File(this::class.java.classLoader.getResource("workflows/lint.yml").file)
        val byteArray = file.readBytes() // Read file contents as byte array
        val workflowContent = Base64.getEncoder().encodeToString(byteArray)
        val submissionFileWorkflow = SubmissionFile("committed by kotlin backend", workflowContent)
        val req: Deferred<ResponseBody> = async { apiClient.pushFileCall(repoName, workflowPath, submissionFileWorkflow) }
        req.await()
        return@coroutineScope req
    }

    /**
     * Push description of the user as the readme to the GitHub repository.
     * @param apiClient the client for GitHub api call
     * @param description the description the user sent
     * @param repoName the owner and name of the repo
     */
    suspend fun pushReadme(apiClient: GitHubApiClient, description: String, repoName: String): Deferred<ResponseBody> = coroutineScope {
        val readmePath = "README.md"
        val readmeContent = Base64.getEncoder().encodeToString(description.toByteArray())
        val submissionFileReadme = SubmissionFile("committed by kotlin backend", readmeContent)
        val req: Deferred<ResponseBody> = async { apiClient.pushFileCall(repoName, readmePath, submissionFileReadme) }
        req.await()
        return@coroutineScope req
    }

    /**
     * Trigger the lintig workflow.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    suspend fun triggerWorkflow(apiClient: GitHubApiClient, repoName: String) = coroutineScope {
        var maxtries = 20
        val workflowName = "lint.yml"
        val branch = "main"
        val workflowDispatch = WorkflowDispatch(branch)
        var req: Response<Unit> = apiClient.triggerWorkflow(repoName, workflowName, workflowDispatch)
        while(!req.isSuccessful && maxtries > 0) {
            req = apiClient.triggerWorkflow(repoName, workflowName, workflowDispatch)
            maxtries -= 1
            delay(500)
        }
        if(maxtries <= 0) {
            throw TriggerWorkflowException("Unable to trigger workflow")
        }
    }

    /**
     * Checks if the there already exists a GitHub Repository for the user.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     * @return the [Boolean] if it exists or not
     */
    fun submissionGitRepositoryExists(apiClient: GitHubApiClient, repoName: String): Boolean {
        val getRepository = apiClient.getSubmissionRepository(repoName).execute()
        return getRepository.isSuccessful
    }

    /**
     * Get the result of the linting workflow.
     * @param userEmail the email of the user who made the submission
     * @return the [LintResultDTO] of the submission repository
     */
    fun getLintingResult(userEmail: String): LintResultDTO {
        return LintResultDTO("")
    }

}