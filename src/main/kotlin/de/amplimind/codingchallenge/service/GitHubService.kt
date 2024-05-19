package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.dto.response.LintResultResponseDTO
import de.amplimind.codingchallenge.exceptions.GitHubApiCallException
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.*
import de.amplimind.codingchallenge.utils.ZipUtils
import de.amplimind.codingchallenge.utils.ApiRequestUtils
import de.amplimind.codingchallenge.utils.SubmissionUtils
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import retrofit2.Response

@Service
class GitHubService(
    private val submissionRepository: SubmissionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO: optimize requests

    /**
     * Upload the code to the Repository.
     * @param apiClient the client for GitHub api calls
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     */
    suspend fun pushToRepo(
        apiClient: GitHubApiClientI,
        submitSolutionRequestDTO: SubmitSolutionRequestDTO,
        userEmail: String,
    ) = coroutineScope {
        pushCode(apiClient, submitSolutionRequestDTO.zipFileContent, userEmail)
        pushReadme(apiClient, submitSolutionRequestDTO, userEmail)
        pushWorkflow(apiClient, userEmail)
    }

    /**
     * Create the GitHub submission repository.
     * @param apiClient the client for GitHub api calls
     * @param userEmail the email of the user who made the submission
     * @return the [Response<CreateRepoResponse>] of the GitHub api call
     */
    suspend fun createRepo(
        apiClient: GitHubApiClientI,
        userEmail: String
    ): Response<CreateRepoResponse> =
        coroutineScope {
            val organisation = "amplimindcc"
            val description = "This is the submission repository of $userEmail"
            val repoName = userEmail.replace('@', '.')
            val submissionRepository = SubmissionGitHubRepository(repoName, description)
            var req: Response<CreateRepoResponse>? = null
            try {
                req = ApiRequestUtils.retry(5) { apiClient.createSubmissionRepository(organisation, submissionRepository) }
            } catch (e: Exception) {
                throw GitHubApiCallException("createRepo failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Push the code to the GitHub repository.
     * @param apiClient the client for GitHub api calls
     * @param multipartFile the code to push
     * @param userEmail the email of the user who made the submission
     * @return the [List<Response<PushFileResponse>>] of the GitHub api calls
     */
    suspend fun pushCode(
        apiClient: GitHubApiClientI,
        multipartFile: MultipartFile,
        userEmail: String,
    ): List<Response<PushFileResponse>> =
        coroutineScope {
            val req: List<Response<PushFileResponse>> =
                ZipUtils.unzipCode(multipartFile).map { entry ->
                    val filePath = entry.key.substringAfter("/")
                    val fileContent = entry.value
                    val repoName = userEmail.replace('@', '.')
                    val committer = Committer(userEmail, userEmail)
                    val commitMessage = "add code"
                    val submissionFileCode = SubmissionFile(commitMessage, fileContent, committer)
                    try {
                        ApiRequestUtils.retry(5) { apiClient.pushFileCall(repoName, filePath, submissionFileCode) }
                    } catch (e: Exception) {
                        throw GitHubApiCallException("pushCode failed at file ${entry.key}: " + e.message)
                    }
                }
            return@coroutineScope req
        }

    /**
     * Push the linting workflow to the GitHub repository.
     * @param apiClient the client for GitHub api calls
     * @param userEmail the email of the user who made the submission
     * @return the [Response<PushFileResponse>] of the GitHub api call
     */
    suspend fun pushWorkflow(
        apiClient: GitHubApiClientI,
        userEmail: String,
    ): Response<PushFileResponse> =
        coroutineScope {
            val workflowPath = ".github/workflows/lint.yml"
            val lintWorkflowYml = SubmissionUtils.getLintWorkflowYml()
            val repoName = userEmail.replace('@', '.')
            val committer = Committer(userEmail, userEmail)
            val commitMessage = "add lint.yml"
            val submissionFileWorkflow = SubmissionFile(commitMessage, lintWorkflowYml, committer)
            var req: Response<PushFileResponse>? = null
            try {
                req = ApiRequestUtils.retry(5) { apiClient.pushFileCall(repoName, workflowPath, submissionFileWorkflow) }
            } catch (e: Exception) {
                println("caught error in pushWorkflow")
                throw GitHubApiCallException("pushWorkflow failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Push description of the user as the readme to the GitHub repository.
     * @param apiClient the client for GitHub api calls
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     * @return the [Response<PushFileResponse>] of the GitHub api call
     */
    suspend fun pushReadme(
        apiClient: GitHubApiClientI,
        submitSolutionRequestDTO: SubmitSolutionRequestDTO,
        userEmail: String,
    ): Response<PushFileResponse> =
        coroutineScope {
            val readmePath = "README.md"
            val readmeContentEncoded = SubmissionUtils.fillReadme(userEmail, submitSolutionRequestDTO)
            val repoName = userEmail.replace('@', '.')
            val committer = Committer(userEmail, userEmail)
            val commitMessage = "add README.md"
            val submissionFileReadme = SubmissionFile(commitMessage, readmeContentEncoded, committer)
            var req: Response<PushFileResponse>? = null
            try {
                req = ApiRequestUtils.retry(5) { apiClient.pushFileCall(repoName, readmePath, submissionFileReadme) }
            } catch (e: Exception) {
                throw GitHubApiCallException("pushReadme failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Trigger the linting workflow.
     * @param apiClient the client for GitHub api calls
     * @param repoName the owner and name of the repo
     * @return the [Response<Void>] of the GitHub api call
     */
    suspend fun triggerLintingWorkflow(
        apiClient: GitHubApiClientI,
        repoName: String
    ): Response<Void> = coroutineScope {
        val workflowName = "lint.yml"
        val branch = "main"
        val workflowDispatch = WorkflowDispatch(branch)
        var req: Response<Void>? = null
        try {
            req= ApiRequestUtils.retry(5) { apiClient.triggerWorkflow(repoName, workflowName, workflowDispatch) }
        } catch (e: Exception) {
            throw GitHubApiCallException("triggerLintingWorkflow failed: " + e.message)
        }
        return@coroutineScope req
    }

    /**
     * Checks if the there already exists a GitHub Repository for the user.
     * @param apiClient the client for GitHub api calls
     * @param repoName the owner and name of the repo
     * @return the [Boolean] if it exists or not
     */
    fun submissionGitRepositoryExists(
        apiClient: GitHubApiClientI,
        repoName: String,
    ): Boolean {
        val getRepository = apiClient.getSubmissionRepository(repoName).execute()
        return getRepository.isSuccessful
    }

    /**
     * Get the result of the linting workflow.
     * @param userEmail the email of the user who made the submission
     * @return the [LintResultResponseDTO] of the submission repository
     */
    fun getLintingResult(userEmail: String): LintResultResponseDTO {
        return LintResultResponseDTO("")
    }

    /**
     * Delete a submission GitHub repository.
     * @param apiClient the client for GitHub api calls
     * @param repoName the owner and name of the repo
     * @return the [Response<Void>] of the GitHub api call
     */
    suspend fun deleteSubmissionRepository(
        apiClient: GitHubApiClientI,
        repoName: String
    ): Response<Void> = coroutineScope {
        var req: Response<Void>? = null
        try {
            req = ApiRequestUtils.retry(10) { apiClient.deleteRepository(repoName) }
        } catch (e: Exception) {
            throw GitHubApiCallException("deleteSubmissionRepository failed: " + e.message)
        }
        return@coroutineScope req
    }
}
