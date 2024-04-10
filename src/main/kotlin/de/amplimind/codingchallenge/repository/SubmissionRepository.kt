package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Submission
import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRepository : JpaRepository<Submission, Long>
