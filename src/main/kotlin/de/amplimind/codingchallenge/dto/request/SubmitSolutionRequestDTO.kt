package de.amplimind.codingchallenge.dto.request

import org.springframework.web.multipart.MultipartFile

/**
 * Data Transfer Object for submitting a Solution
 * @param description the description the user sent
 * @param zipFileContent the code the user wants to submit
 */
data class SubmitSolutionRequestDTO(
    val description: String,
    val zipFileContent: MultipartFile,
)
