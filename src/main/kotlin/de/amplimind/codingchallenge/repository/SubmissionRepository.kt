package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Submission
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for managing [Submission]s
 */
interface SubmissionRepository : JpaRepository<Submission, Long> {
    /**
     * Finds a submission by its user email
     * @param userEmail the email to search for
     * @return the submission if found, null otherwise
     */
    fun findByUserEmail(userEmail: String): Submission?

    /**
     * Finds all submissions by project id
     * @param projectId the id of the project
     * @return a list of submissions
     */
    fun findByProjectID(projectId: Long): List<Submission>
}
