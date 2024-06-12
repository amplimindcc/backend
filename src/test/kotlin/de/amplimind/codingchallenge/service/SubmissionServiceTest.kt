package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.GitHubApiCallException
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.exceptions.SolutionAlreadySubmittedException
import de.amplimind.codingchallenge.exceptions.TooLateSubmissionException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.submission.CreateBlobResponse
import de.amplimind.codingchallenge.submission.CreateCommitResponse
import de.amplimind.codingchallenge.submission.CreateRepoResponse
import de.amplimind.codingchallenge.submission.CreateTreeResponse
import de.amplimind.codingchallenge.submission.GetGitTreeResponse
import de.amplimind.codingchallenge.submission.GitHubApiClient
import de.amplimind.codingchallenge.submission.PushFileResponse
import de.amplimind.codingchallenge.submission.UpdateBranchReferenceResponse
import de.amplimind.codingchallenge.utils.UserUtils
import de.amplimind.codingchallenge.utils.ZipUtils
import de.amplimind.codingchallenge.utils.ZipUtils.unzipCode
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.multipart.MultipartFile
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

/**
 * Test class for [SubmissionService]
 */
internal class SubmissionServiceTest {
    @MockK
    private lateinit var submissionRepository: SubmissionRepository

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @MockK
    private lateinit var gitHubApiClient: GitHubApiClient

    @MockK
    private lateinit var getSubmissionRepositoryCall: Call<Result<String>>

    @MockK
    private lateinit var getSubmissionRepositoryResponse: Response<Result<String>>

    @MockK
    private lateinit var createRepoResponse: Response<CreateRepoResponse>

    @MockK
    private lateinit var pushFileResponse: Response<PushFileResponse>

    @MockK
    private lateinit var triggerWorkflowResponse: Response<Void>

    @MockK
    private lateinit var deleteRepositoryResponse: Response<Void>

    @MockK
    private lateinit var createBlobResponse: Response<CreateBlobResponse>

    @MockK
    private lateinit var blobResponse: CreateBlobResponse

    @MockK
    private lateinit var createTreeResponse: Response<CreateTreeResponse>

    @MockK
    private lateinit var treeResponse: CreateTreeResponse

    @MockK
    private lateinit var createCommitResponse: Response<CreateCommitResponse>

    @MockK
    private lateinit var commitResponse: CreateCommitResponse

    @MockK
    private lateinit var updateBranchReferenceResponse: Response<UpdateBranchReferenceResponse>

    @MockK
    private lateinit var getGitTreeResponse: Response<GetGitTreeResponse>

    @MockK
    private lateinit var gitTreeResponse: GetGitTreeResponse

    @MockK
    private lateinit var multipartFile: MultipartFile

    @MockK
    private lateinit var userUtils: UserUtils

    @MockK
    private lateinit var zipUtils: ZipUtils

    @MockK
    private lateinit var userDetails: UserDetails

    @MockK
    private lateinit var authentication: Authentication

    @MockK
    private lateinit var securityContext: SecurityContext

    @InjectMockKs
    private lateinit var gitHubService: GitHubService

    @InjectMockKs
    private lateinit var submissionService: SubmissionService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    fun init_mocks_test_submit_code(): SubmitSolutionRequestDTO {
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { authentication.principal } returns userDetails
        every { userDetails.username } returns "user@web.de"

        val description = "Sample description"
        val language = "Kotlin"
        val version = "1.0"
        val zipFileContent = multipartFile
        val submitSolutionRequestDTO = SubmitSolutionRequestDTO(description, language, version, zipFileContent)

        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.IN_IMPLEMENTATION,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now().plus(Duration.ofHours(1))),
            )

        val unzipCodeMap: Map<String, String> =
            mapOf(
                "file1" to "code1",
                "file2" to "code2",
                "file3" to "code3",
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        mockkObject(ZipUtils)
        every { ZipUtils.unzipCode(any()) } returns unzipCodeMap
        return submitSolutionRequestDTO
    }

    /**
     * Test that a submission is submitted.
     */
    @Test
    fun test_submit_code() {
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { authentication.principal } returns userDetails
        every { userDetails.username } returns "user@web.de"

        val description = "Sample description"
        val language = "Kotlin"
        val version = "1.0"
        val zipFileContent = multipartFile
        val submitSolutionRequestDTO = SubmitSolutionRequestDTO(description, language, version, zipFileContent)

        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.IN_IMPLEMENTATION,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now().plus(Duration.ofHours(1))),
            )

        val unzipCodeMap: Map<String, String> =
            mapOf(
                "file1" to "code1",
                "file2" to "code2",
                "file3" to "code3",
            )

        val dummySha = "123"

        every { submissionRepository.findByUserEmail(any()) } returns submission

        mockkObject(ZipUtils)
        every { ZipUtils.unzipCode(any()) } returns unzipCodeMap

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.pushFileCall(any(), any(), any()) } returns pushFileResponse
        every { pushFileResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        every { createBlobResponse.body() } returns blobResponse
        every { blobResponse.sha } returns dummySha

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns true

        every { getGitTreeResponse.body() } returns gitTreeResponse
        every { gitTreeResponse.sha } returns dummySha

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns true

        every { createTreeResponse.body() } returns treeResponse
        every { treeResponse.sha } returns dummySha

        coEvery { gitHubApiClient.createCommit(any(), any()) } returns createCommitResponse
        every { createCommitResponse.isSuccessful } returns true

        every { createCommitResponse.body() } returns commitResponse
        every { commitResponse.sha } returns dummySha

        coEvery { gitHubApiClient.updateBranchReference(any(), any(), any()) } returns updateBranchReferenceResponse
        every { updateBranchReferenceResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.triggerWorkflow(any(), any(), any()) } returns triggerWorkflowResponse
        every { triggerWorkflowResponse.isSuccessful } returns true

        val savedSubmissionSlot = slot<Submission>()

        every { submissionRepository.save(capture(savedSubmissionSlot)) } returns any()

        every { eventPublisher.publishEvent(any()) } just Runs

        val updatedSubmission = this.submissionService.submitCode(submitSolutionRequestDTO)

        val savedSubmission = savedSubmissionSlot.captured
        assert(savedSubmission.userEmail == submission.userEmail)
        assert(savedSubmission.status == SubmissionStates.SUBMITTED)
        assert(savedSubmission.turnInDate == submission.turnInDate)
        assert(savedSubmission.projectID == submission.projectID)
        assert(savedSubmission.expirationDate == submission.expirationDate)

        assert(updatedSubmission.userEmail == submission.userEmail)
        assert(updatedSubmission.status == SubmissionStates.SUBMITTED)
        assert(updatedSubmission.turnInDate == submission.turnInDate)
        assert(updatedSubmission.projectID == submission.projectID)
        assert(updatedSubmission.expirationDate == submission.expirationDate)
    }

    /**
     * Test that an exception is thrown if the submission is not in the correct state.
     */
    @Test
    fun test_submit_code_failure_state_not_in_implementation() {
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { authentication.principal } returns userDetails
        every { userDetails.username } returns "user@web.de"

        val description = "Sample description"
        val language = "Kotlin"
        val version = "1.0"
        val zipFileContent = multipartFile
        val submitSolutionRequestDTO = SubmitSolutionRequestDTO(description, language, version, zipFileContent)

        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.SUBMITTED,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now().plus(Duration.ofHours(1))),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assertThrows<IllegalStateException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if the submission was too late.
     */
    @Test
    fun test_submit_code_failure_submission_too_late() {
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { authentication.principal } returns userDetails
        every { userDetails.username } returns "user@web.de"

        val description = "Sample description"
        val language = "Kotlin"
        val version = "1.0"
        val zipFileContent = multipartFile
        val submitSolutionRequestDTO = SubmitSolutionRequestDTO(description, language, version, zipFileContent)

        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.IN_IMPLEMENTATION,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now().minus(Duration.ofHours(1))),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assertThrows<TooLateSubmissionException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if the submission repository with the user's name already exists.
     */
    @Test
    fun test_submit_code_failure_repository_already_exists() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns true

        assertThrows<SolutionAlreadySubmittedException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if creating the submission repository fails.
     */
    @Test
    fun test_submit_code_failure_create_repo_api_call_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if pushing files to the submission repository fails.
     */
    @Test
    fun test_submit_code_failure_create_blob_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if getting the git tree fails
     */
    @Test
    fun test_submit_code_failure_get_git_tree_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if creating the git tree fails
     */
    @Test
    fun test_submit_code_failure_create_tree_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if creating a commit fails
     */
    @Test
    fun test_submit_code_failure_create_commit_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createCommit(any(), any()) } returns createCommitResponse
        every { createCommitResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if updating the branch reference fails
     */
    @Test
    fun test_submit_code_failure_update_branch_reference_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createCommit(any(), any()) } returns createCommitResponse
        every { createCommitResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.updateBranchReference(any(), any(), any()) } returns updateBranchReferenceResponse
        every { updateBranchReferenceResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if triggering the linting workflow of the submission repository fails.
     */
    @Test
    fun test_submit_code_failure_trigger_linting_workflow_api_call_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.pushFileCall(any(), any(), any()) } returns pushFileResponse
        every { pushFileResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.createCommit(any(), any()) } returns createCommitResponse
        every { createCommitResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.updateBranchReference(any(), any(), any()) } returns updateBranchReferenceResponse
        every { updateBranchReferenceResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.triggerWorkflow(any(), any(), any()) } returns triggerWorkflowResponse
        every { triggerWorkflowResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that an exception is thrown if deleting the submission repository fails.
     */
    @Test
    fun test_submit_code_failure_delete_repository_api_call_fails() {
        val submitSolutionRequestDTO = init_mocks_test_submit_code()

        every { gitHubApiClient.getSubmissionRepository(any()) } returns getSubmissionRepositoryCall
        every { getSubmissionRepositoryCall.execute() } returns getSubmissionRepositoryResponse
        every { getSubmissionRepositoryResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createSubmissionRepository(any(), any()) } returns createRepoResponse
        every { createRepoResponse.isSuccessful } returns true

        coEvery { gitHubApiClient.pushFileCall(any(), any(), any()) } returns pushFileResponse
        every { pushFileResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createBlob(any(), any()) } returns createBlobResponse
        every { createBlobResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.getGitTree(any(), any()) } returns getGitTreeResponse
        every { getGitTreeResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createTree(any(), any()) } returns createTreeResponse
        every { createTreeResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.createCommit(any(), any()) } returns createCommitResponse
        every { createCommitResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.updateBranchReference(any(), any(), any()) } returns updateBranchReferenceResponse
        every { updateBranchReferenceResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.triggerWorkflow(any(), any(), any()) } returns triggerWorkflowResponse
        every { triggerWorkflowResponse.isSuccessful } returns false

        coEvery { gitHubApiClient.deleteRepository(any()) } returns deleteRepositoryResponse
        every { deleteRepositoryResponse.isSuccessful } returns false

        assertThrows<GitHubApiCallException> { this.submissionService.submitCode(submitSolutionRequestDTO) }
    }

    /**
     * Test that a submission state is changed to reviewed.
     */
    @Test
    fun test_change_submission_state_reviewed() {
        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.SUBMITTED,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now()),
            )

        val emailSlot = slot<String>()

        every { submissionRepository.findByUserEmail(capture(emailSlot)) } returns submission

        val savedSubmissionSlot = slot<Submission>()

        every { submissionRepository.save(capture(savedSubmissionSlot)) } returns any()

        every { eventPublisher.publishEvent(any()) } just Runs

        val updatedSubmission = this.submissionService.changeSubmissionStateReviewed(submission.userEmail)

        val savedSubmission = savedSubmissionSlot.captured
        assert(savedSubmission.userEmail == submission.userEmail)
        assert(savedSubmission.status == SubmissionStates.REVIEWED)
        assert(savedSubmission.turnInDate == submission.turnInDate)
        assert(savedSubmission.projectID == submission.projectID)
        assert(savedSubmission.expirationDate == submission.expirationDate)

        // Correct email should be used
        assert(emailSlot.captured == submission.userEmail)

        // The submission should be updated to reviewed
        assert(updatedSubmission.userEmail == submission.userEmail)
        assert(updatedSubmission.status == SubmissionStates.REVIEWED)
        assert(updatedSubmission.turnInDate == submission.turnInDate)
        assert(updatedSubmission.projectID == submission.projectID)
        assert(updatedSubmission.expirationDate == submission.expirationDate)
    }

    /**
     * Test that an exception is thrown if the submission is not found.
     */
    @Test
    fun test_change_submission_state_reviewed_failure() {
        every { submissionRepository.findByUserEmail(any()) } returns null

        assertThrows<ResourceNotFoundException> { this.submissionService.changeSubmissionStateReviewed("unknown@web.de") }
    }

    /**
     * Test that an exception is thrown if the submission is not in the correct state.
     */
    @Test
    fun test_change_submission_state_reviewed_failure_wrong_state_init() {
        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.INIT,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now()),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assertThrows<IllegalStateException> { this.submissionService.changeSubmissionStateReviewed("user@web.de") }
    }

    /**
     * Test that an exception is thrown if the submission is not in the correct state.
     */
    @Test
    fun test_change_submission_state_reviewed_failure_wrong_state_in_implementation() {
        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.IN_IMPLEMENTATION,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now()),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assertThrows<IllegalStateException> { this.submissionService.changeSubmissionStateReviewed("user@web.de") }
    }

    /**
     * Test that an exception is thrown if the submission is not in the correct state.
     */
    @Test
    fun test_change_submission_state_reviewed_failure_wrong_state_reviewed() {
        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.REVIEWED,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now()),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assertThrows<IllegalStateException> { this.submissionService.changeSubmissionStateReviewed("user@web.de") }
    }

    /**
     * Test that the correct project ID for a user is fetched.
     */
    @Test
    fun test_get_project_id_of_user_works() {
        val submission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.REVIEWED,
                turnInDate = Timestamp.from(Instant.now()),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now()),
            )

        every { submissionRepository.findByUserEmail(any()) } returns submission

        assert(submissionService.getProjectIdOfUser("user@web.de") == 1L)
    }

    /**
     * Test that an exception is thrown if the user supplied does not have a submission.
     */
    @Test
    fun test_get_project_id_of_user_fails() {
        every { submissionRepository.findByUserEmail(any()) } returns null

        assertThrows<ResourceNotFoundException> { submissionService.getProjectIdOfUser("notexistent@web.de") }
    }

    /**
     * Test that all submissions come back and are mapped properly
     */
    @Test
    fun test_fetch_all_submissions() {
        val commonSubmission =
            Submission(
                userEmail = "user@web.de",
                status = SubmissionStates.SUBMITTED,
                turnInDate = Timestamp.from(Instant.now().minusSeconds(60)),
                projectID = 1,
                expirationDate = Timestamp.from(Instant.now().plusSeconds(6000)),
            )

        val commonSubmission2 =
            Submission(
                userEmail = "another@web.de",
                status = SubmissionStates.SUBMITTED,
                turnInDate = Timestamp.from(Instant.now().minusSeconds(60)),
                projectID = 2,
                expirationDate = Timestamp.from(Instant.now().plusSeconds(6000)),
            )
        val submissions = listOf(commonSubmission, commonSubmission2)

        every { submissionRepository.findAll() } returns submissions

        val result = submissionService.fetchAllSubmissions()

        assertEquals(2, result.size)

        assertEquals("user@web.de", result[0].userEmail)
        assertEquals(SubmissionStates.SUBMITTED, result[0].status)
        assertEquals(1, result[0].projectID)
        assertNotNull(result[0].turnInDate)
        assertNotNull(result[0].expirationDate)

        assertEquals("another@web.de", result[1].userEmail)
        assertEquals(SubmissionStates.SUBMITTED, result[1].status)
        assertEquals(2, result[1].projectID)
        assertNotNull(result[1].turnInDate)
        assertNotNull(result[1].expirationDate)
    }

    /**
     * Test that when submissions are empty result is empty
     */
    @Test
    fun test_fetch_all_submissions_empty_submissions() {
        every { submissionRepository.findAll() } returns emptyList()

        val result = submissionService.fetchAllSubmissions()

        assert(result.isEmpty())
    }
}
