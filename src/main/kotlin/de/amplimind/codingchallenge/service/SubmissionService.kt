package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.repository.SubmissionRepository
import org.springframework.stereotype.Service

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
)
