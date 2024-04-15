package de.amplimind.codingchallenge.dto.request

import org.springframework.web.multipart.MultipartFile

data class SubmitSolutionRequestDTO (
    val description: String,
    val zipFileContent: MultipartFile
)