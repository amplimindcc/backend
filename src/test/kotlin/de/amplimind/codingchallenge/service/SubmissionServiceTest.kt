package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.springframework.context.ApplicationEventPublisher
import java.sql.Timestamp
import java.time.Instant

/**
 * Test class for [SubmissionService]
 */
internal class SubmissionServiceTest {
    @MockK
    private lateinit var submissionRepository: SubmissionRepository

    @MockK
    private lateinit var gitHubService: GitHubService

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var submissionService: SubmissionService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

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
        val commonSubmission = Submission(
            userEmail = "user@web.de",
            status = SubmissionStates.SUBMITTED,
            turnInDate = Timestamp.from(Instant.now().minusSeconds(60)),
            projectID = 1,
            expirationDate = Timestamp.from(Instant.now().plusSeconds(6000))
        )

        val commonSubmission2 = Submission(
            userEmail = "another@web.de",
            status = SubmissionStates.SUBMITTED,
            turnInDate = Timestamp.from(Instant.now().minusSeconds(60)),
            projectID = 2,
            expirationDate = Timestamp.from(Instant.now().plusSeconds(6000))
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
    fun  test_fetch_all_submissions_empty_submissions() {
        every { submissionRepository.findAll() } returns emptyList()

        val result = submissionService.fetchAllSubmissions()

        assert(result.isEmpty())
    }
}
