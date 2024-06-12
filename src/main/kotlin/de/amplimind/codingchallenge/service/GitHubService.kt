package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.dto.response.LintResultResponseDTO
import de.amplimind.codingchallenge.exceptions.GitHubApiCallException
import de.amplimind.codingchallenge.exceptions.LinterResultNotAvailableException
import de.amplimind.codingchallenge.exceptions.UnzipException
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.Blob
import de.amplimind.codingchallenge.submission.Committer
import de.amplimind.codingchallenge.submission.CreateCommitRequest
import de.amplimind.codingchallenge.submission.CreateRepoResponse
import de.amplimind.codingchallenge.submission.CreateTreeRequest
import de.amplimind.codingchallenge.submission.GetGitTreeResponse
import de.amplimind.codingchallenge.submission.GitHubApiClient
import de.amplimind.codingchallenge.submission.PushFileResponse
import de.amplimind.codingchallenge.submission.SubmissionFile
import de.amplimind.codingchallenge.submission.SubmissionGitHubRepository
import de.amplimind.codingchallenge.submission.TreeItem
import de.amplimind.codingchallenge.submission.UpdateBranchReferenceRequest
import de.amplimind.codingchallenge.submission.WorkflowDispatch
import de.amplimind.codingchallenge.utils.ApiRequestUtils
import de.amplimind.codingchallenge.utils.SubmissionUtils
import de.amplimind.codingchallenge.utils.ZipUtils
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import retrofit2.Response
import java.util.zip.ZipInputStream

/**
 * Service class responsible for handling the GitHub API calls.
 */
@Service
class GitHubService(
    private val submissionRepository: SubmissionRepository,
    private val gitHubApiClient: GitHubApiClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO: optimize requests

    /**
     * Upload the code to the Repository.
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     */
    suspend fun pushToRepo(
        submitSolutionRequestDTO: SubmitSolutionRequestDTO,
        userEmail: String,
    ) = coroutineScope {
        pushWorkflow(userEmail)
        val codeTreeItems: List<TreeItem> = createCodeBlobs(submitSolutionRequestDTO.zipFileContent, userEmail)
        val readmeTreeItem: TreeItem = createReadmeBlob(submitSolutionRequestDTO, userEmail)
        val readmeTreeItemList = listOf(readmeTreeItem) ?: emptyList()
        val allTreeItems: List<TreeItem> = readmeTreeItemList + codeTreeItems
        val repo = userEmail.replace('@', '.')
        val gitBaseTree: Response<GetGitTreeResponse> = ApiRequestUtils.retry(5) { gitHubApiClient.getGitTree(repo, "main") }
        val baseTreeSha = gitBaseTree.body()?.sha
        if (baseTreeSha != null) {
            val treeResponse = ApiRequestUtils.retry(5) { gitHubApiClient.createTree(repo, CreateTreeRequest(allTreeItems, baseTreeSha)) }
            val treeSha = treeResponse.body()?.sha
            if (treeSha != null) {
                val commitMessage = "submit code"
                val committer = Committer(userEmail, userEmail)
                val commitResponse =
                    ApiRequestUtils.retry(
                        5,
                    ) { gitHubApiClient.createCommit(repo, CreateCommitRequest(commitMessage, treeSha, committer)) }
                val commitSha = commitResponse.body()?.sha
                if (commitSha != null) {
                    val branch = "main"
                    ApiRequestUtils.retry(
                        5,
                    ) { gitHubApiClient.updateBranchReference(repo, branch, UpdateBranchReferenceRequest(commitSha)) }
                }
            }
        }
    }

    /**
     * Push the linting workflow to the GitHub repository.
     * @param userEmail the email of the user who made the submission
     * @return the [Response<PushFileResponse>] of the GitHub api call
     */
    suspend fun pushWorkflow(userEmail: String): Response<PushFileResponse> =
        coroutineScope {
            val workflowPath = ".github/workflows/lint.yml"
            val lintWorkflowYml = SubmissionUtils.getLintWorkflowYml()
            val committer = Committer("kotlin backend", "kotline backend")
            val commitMessage = "add lint.yml"
            val repoName = userEmail.replace('@', '.')
            val submissionFileWorkflow = SubmissionFile(commitMessage, lintWorkflowYml, committer)
            var req: Response<PushFileResponse>? = null
            try {
                req = ApiRequestUtils.retry(5) { gitHubApiClient.pushFileCall(repoName, workflowPath, submissionFileWorkflow) }
            } catch (e: Exception) {
                throw GitHubApiCallException("pushWorkflow failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Create the GitHub submission repository.
     * @param userEmail the email of the user who made the submission
     * @return the [Response<CreateRepoResponse>] of the GitHub api call
     */
    suspend fun createRepo(userEmail: String): Response<CreateRepoResponse> =
        coroutineScope {
            val organisation = "amplimindcc"
            val description = "This is the submission repository of $userEmail"
            val repoName = userEmail.replace('@', '.')
            val submissionRepository = SubmissionGitHubRepository(repoName, description)
            var req: Response<CreateRepoResponse>? = null
            try {
                req = ApiRequestUtils.retry(5) { gitHubApiClient.createSubmissionRepository(organisation, submissionRepository) }
            } catch (e: Exception) {
                throw GitHubApiCallException("createRepo failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Create the blobs of the files to push.
     * @param multipartFile the code to push
     * @param userEmail the email of the user who made the submission
     * @return the [List<TreeItem>] of the files to push
     */
    suspend fun createCodeBlobs(
        multipartFile: MultipartFile,
        userEmail: String,
    ): List<TreeItem> =
        coroutineScope {
            val treeItems: MutableList<TreeItem> = mutableListOf()
            val files: Map<String, String> = ZipUtils.unzipCode(multipartFile)
            val repoName = userEmail.replace('@', '.')
            for ((filepath, content) in files) {
                val filePath = filepath.substringAfter("/")
                val blob = Blob(content)
                try {
                    val blobResponse = ApiRequestUtils.retry(5) { gitHubApiClient.createBlob(repoName, blob) }
                    treeItems.add(TreeItem(filePath, "100644", "blob", blobResponse.body()?.sha))
                } catch (e: Exception) {
                    throw GitHubApiCallException("pushCode failed at file $filepath: " + e.message)
                }
            }

            return@coroutineScope treeItems
        }

    /**
     * Create a blob for the readme file
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     * @return the [TreeItem] of the readme file
     */
    suspend fun createReadmeBlob(
        submitSolutionRequestDTO: SubmitSolutionRequestDTO,
        userEmail: String,
    ): TreeItem =
        coroutineScope {
            val readmePath = "README.md"
            val readmeContentEncoded = SubmissionUtils.fillReadme(userEmail, submitSolutionRequestDTO)
            val repoName = userEmail.replace('@', '.')
            val blob = Blob(readmeContentEncoded)
            val readmeTreeItem: TreeItem
            try {
                val blobResponse = ApiRequestUtils.retry(5) { gitHubApiClient.createBlob(repoName, blob) }
                readmeTreeItem = TreeItem(readmePath, "100644", "blob", blobResponse.body()?.sha)
            } catch (e: Exception) {
                throw GitHubApiCallException("pushReadme failed: " + e.message)
            }
            return@coroutineScope readmeTreeItem
        }

    /**
     * Trigger the linting workflow.
     * @param repoName the owner and name of the repo
     * @return the [Response<Void>] of the GitHub api call
     */
    suspend fun triggerLintingWorkflow(repoName: String): Response<Void> =
        coroutineScope {
            val workflowName = "lint.yml"
            val branch = "main"
            val workflowDispatch = WorkflowDispatch(branch)
            var req: Response<Void>? = null
            try {
                req = ApiRequestUtils.retry(5) { gitHubApiClient.triggerWorkflow(repoName, workflowName, workflowDispatch) }
            } catch (e: Exception) {
                throw GitHubApiCallException("triggerLintingWorkflow failed: " + e.message)
            }
            return@coroutineScope req
        }

    /**
     * Checks if the there already exists a GitHub Repository for the user.
     * @param repoName the owner and name of the repo
     * @return the [Boolean] if it exists or not
     */
    fun submissionGitRepositoryExists(repoName: String): Boolean {
        val getRepository = gitHubApiClient.getSubmissionRepository(repoName).execute()
        return getRepository.isSuccessful
    }

    /**
     * Get the result of the linting workflow.
     * @param userEmail the email of the user who made the submission
     * @return the [LintResultResponseDTO] of the submission repository
     */
    suspend fun getLintingResult(userEmail: String): LintResultResponseDTO {
        val repoName = userEmail.replace('@', '.')
        val artifactsResponse = gitHubApiClient.getArtifacts(repoName)

        val lintLogArtifact =
            artifactsResponse.artifacts.find { it.name == "MegaLinter reports" }
                ?: throw LinterResultNotAvailableException("Error while getting artifact: MegaLinter reports not found")

        val downloadArtifact = gitHubApiClient.downloadArtifact(repoName, lintLogArtifact.id)
        if (!downloadArtifact.isSuccessful) {
            throw LinterResultNotAvailableException("Error while downloading artifact: ${downloadArtifact.message()}")
        }

        val zipFile: ZipInputStream
        try {
            zipFile = ZipUtils.openZipFile(downloadArtifact.body()!!.byteStream())
        } catch (e: Exception) {
            throw UnzipException("Error while unzipping the file: ${e.message}")
        }
        val lintResult = ZipUtils.readLintResult(zipFile)

        return LintResultResponseDTO(lintResult)
    }

    /**
     * Delete a submission GitHub repository.
     * @param repoName the owner and name of the repo
     * @return the [Response<Void>] of the GitHub api call
     */
    suspend fun deleteSubmissionRepository(repoName: String): Response<Void> =
        coroutineScope {
            var req: Response<Void>? = null
            try {
                req = ApiRequestUtils.retry(10) { gitHubApiClient.deleteRepository(repoName) }
            } catch (e: Exception) {
                throw GitHubApiCallException("deleteSubmissionRepository failed: " + e.message)
            }
            return@coroutineScope req
        }
}
