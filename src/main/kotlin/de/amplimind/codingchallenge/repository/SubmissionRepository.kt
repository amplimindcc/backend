package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Submission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubmissionRepository:JpaRepository<Submission,UUID>