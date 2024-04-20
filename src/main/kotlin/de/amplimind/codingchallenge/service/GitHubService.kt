package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.LintResultDTO
import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.*
import de.amplimind.codingchallenge.utils.ZipUtils
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

@Service
class GitHubService (
    private val submissionRepository: SubmissionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Upload the code to the Repository.
     * @param submitSolutionRequestDTO the request to upload the code
     * @param repoName the email of the user who made the submission
     */
    // TODO: optimize requests
    fun pushToRepo(apiClient: GitHubApiClient, submitSolutionRequestDTO: SubmitSolutionRequestDTO, repoName: String) {
        pushCode(apiClient, submitSolutionRequestDTO.zipFileContent, repoName)
        pushWorkflow(apiClient, repoName)
        pushReadme(apiClient, submitSolutionRequestDTO.description, repoName)
    }

    /**
     * Create the GitHub submission repository.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    fun createRepo(apiClient: GitHubApiClient, repoName: String) {
        val organisation = "amplimindcc"
        val description = "This is the submission repository of $repoName"
        val submissionRepository = SubmissionGitHubRepository(repoName, description)
        apiClient.createSubmissionRepository(organisation, submissionRepository).execute()
    }

    /**
     * Push the code to the GitHub repository.
     * @param apiClient the client for GitHub api alls
     * @param submitSolutionRequestDTO the request to upload the code
     * @param repoName the owner and name of the repo
     */
    fun pushCode(apiClient: GitHubApiClient, multipartFile: MultipartFile, repoName: String) {
            ZipUtils.unzipCode(multipartFile).map { entry ->
                val filePath = entry.key.substringAfter("/")
                val fileContent = entry.value
                val submissionFileCode = SubmissionFile("committed by kotlin backend", fileContent)
                apiClient.pushFileCall(repoName, filePath, submissionFileCode).execute()
            }
    }

    /**
     * Push the linting workflow to the GitHub repository.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    fun pushWorkflow(apiClient: GitHubApiClient, repoName: String) {
        val workflowPath = ".github/workflows/lint.yml"
        val file = File(this::class.java.classLoader.getResource("workflows/lint.yml").file)
        val byteArray = file.readBytes() // Read file contents as byte array
        val workflowContent = Base64.getEncoder().encodeToString(byteArray)
        val submissionFileWorkflow = SubmissionFile("committed by kotlin backend", workflowContent)
        apiClient.pushFileCall(repoName, workflowPath, submissionFileWorkflow).execute()
    }

    /**
     * Push description of the user as the readme to the GitHub repository.
     * @param apiClient the client for GitHub api call
     * @param description the description the user sent
     * @param repoName the owner and name of the repo
     */
    fun pushReadme(apiClient: GitHubApiClient, description: String, repoName: String) {
        val readmePath = "README.md"
        val readmeContent = Base64.getEncoder().encodeToString(description.toByteArray())
        val submissionFileReadme = SubmissionFile("committed by kotlin backend", readmeContent)
        apiClient.pushFileCall(repoName, readmePath, submissionFileReadme).execute()
    }

    /**
     * Trigger the lintig workflow.
     * @param apiClient the client for GitHub api alls
     * @param repoName the owner and name of the repo
     */
    fun triggerWorkflow(apiClient: GitHubApiClient, repoName: String) {
        val workflowName = "lint.yml"
        val branch = "main"
        val workflowDispatch = WorkflowDispatch(branch)
        apiClient.triggerWorkflow(repoName, workflowName, workflowDispatch).execute()
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