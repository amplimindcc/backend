package de.amplimind.codingchallenge.dto.request

data class SubmitSolutionRequestDTO (
    val description: String,
    val zipFileContent: ByteArray
)