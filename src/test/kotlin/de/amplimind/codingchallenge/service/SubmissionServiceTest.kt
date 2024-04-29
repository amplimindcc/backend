package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
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
}
