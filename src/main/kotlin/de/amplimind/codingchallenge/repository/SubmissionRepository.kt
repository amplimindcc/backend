package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Submission
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for managing [Submission]s
 */
interface SubmissionRepository : JpaRepository<Submission, Long> {
    fun findByUserEmail(userEmail: String): Submission?

    fun findByProjectID(projectId: Long): List<Submission>
}
