package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.extensions.DTOExtensions.toSumbissionInfoDTO
import de.amplimind.codingchallenge.extensions.EnumExtensions.matchesAny
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.SubmissionRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.sql.Timestamp
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.coroutines.EmptyCoroutineContext


/**
 * Service for managing submissions.
 */
@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Adds a new Submission.
     * @param userEmail the email of the user who made the submission
     */
    suspend fun submitCode(submitSolutionRequestDTO: SubmitSolutionRequestDTO, userEmail: String) {
        val submission = this.submissionRepository.findByUserEmail(userEmail)
            ?:throw ResourceNotFoundException("Submission with email $userEmail was not found");

        // TODO: check if submission is too late

        uploadCodeToRepository(submitSolutionRequestDTO, userEmail)

        // TODO: check linting with github api

        val updatedSubmission =
            submission.let {
                Submission(
                    userEmail = it.userEmail,
                    expirationDate = it.expirationDate,
                    projectID = it.projectID,
                    turnInDate = Timestamp(System.currentTimeMillis()),
                    status = SubmissionStates.SUBMITTED,
                )
            }

        this.submissionRepository.save(updatedSubmission)
    }

    /**
     * Upload the code to the Repository.
     * @param submitSolutionRequestDTO the request to upload the code
     * @param userEmail the email of the user who made the submission
     */
    // TODO: optimize requests
    suspend fun uploadCodeToRepository(submitSolutionRequestDTO: SubmitSolutionRequestDTO, userEmail: String) {
        if(!submissionGitRepositoryExists(userEmail)) {
            createSubmissionRepository(userEmail);
        }
        val owner = userEmail.split("@")[0]
        val accessToken = "GitHub Access Token"
        runBlocking(context = EmptyCoroutineContext) {
            unzipCode(submitSolutionRequestDTO.zipFileContent).forEach { entry ->
                launch {
                    val filepathWithoutFilename = entry.key.substring(entry.key.indexOf("/", 0), entry.key.lastIndexOf("/")).ifEmpty { "/" }
                    val fileContent = entry.value
                    makePutRequest(owner, filepathWithoutFilename, fileContent, accessToken)
                }
            }
        }
    }

    /**
     * Checks if the there already exists a GitHub Repository for the user.
     * @param userEmail the email of the user who made the submission
     * @return the [Boolean] if it exists or not
     */
    fun submissionGitRepositoryExists(userEmail: String): Boolean {
        val submission = this.submissionRepository.findByUserEmail(userEmail)
            ?:throw ResourceNotFoundException("Submission with email ${userEmail} was not found");
        return submission.status != SubmissionStates.INIT && submission.status != SubmissionStates.IN_IMPLEMENTATION;
    }

    /**
     * Upload the code to the Repository.
     * @param owner the owner of the repository
     * @param filePath the filepath where the file should reside in the repository
     * @param fileContent the content of the file that should be pushed
     * @param accessToken the personal access token used for authentication
     */
    suspend fun makePutRequest(owner: String, filePath: String, fileContent: String, accessToken: String) {
        withContext(Dispatchers.IO) {
            val url = URI("https://api.github.com/repos/amplimindcc/${owner}/contents${filePath}/").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonPayload = "{\"content\": \"${fileContent}\"}"
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
            }
            connection.disconnect()
        }
    }

    /**
     * Upload the code to the Repository.
     * @param filePath the owner of the repository
     * @return the [ByteArray] of the file
     */
    fun zipToByteArray(filePath: String): ByteArray {
        val currentDirectory = System.getProperty("user.dir") // Get the current directory
        val zipFile = File(currentDirectory, filePath)
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            zipFile.inputStream().use { input ->
                input.copyTo(byteArrayOutputStream)
            }
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray
        } catch (e: Exception) {
            throw e
        }

    }

    /**
     * Unzip the zip file the user submitted
     * @param zipFileContent the content of the zip file
     * @return the [Map] of the files and their content that should be pushed to the Repository
     */
    fun unzipCode(zipFileContent: MultipartFile): Map<String, String> {
        val files = mutableMapOf<String, String>()
        try {
            ZipInputStream(zipFileContent.inputStream).use { zipInputStream ->
                traverseFolder(zipInputStream, files)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return files
    }

    /**
     * recursively traverse through the zip folder
     * @param zipInputStream the zipfile
     * @param files a map of the files and their paths
     * @return the [Map] of the files and their content base64 encoded
     */
    fun traverseFolder(zipInputStream: ZipInputStream, files: MutableMap<String, String>): MutableMap<String, String> {
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if(!entry.isDirectory) {
                files[entry.name] = Base64.getEncoder().encodeToString(zipInputStream.readBytes())
            } else {
                traverseFolder(zipInputStream, files)
            }
            entry = zipInputStream.nextEntry
        }
        return files
    }

    /**
     * Adds a new Submission Repository.
     * @param userEmail the email of the user who made the submission
     */
    fun createSubmissionRepository(userEmail: String) {

    }
}
) {
    /**
     * Changes the submission state to reviewed.
     * @param email the email of the user which will be used to find the submission
     * @return the [SubmissionInfoDTO] of the updated submission
     */
    fun changeSubmissionStateReviewed(email: String): SubmissionInfoDTO {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("If the submission for the provided $email was not found.")

        if (submission.status.matchesAny(SubmissionStates.SUBMITTED).not()) {
            // Submission has to be submitted
            throw IllegalStateException("Submission is in state ${submission.status} and can not be changed reviewed.")
        }

        val updatedSubmission =
            submission.let {
                Submission(
                    userEmail = it.userEmail,
                    status = SubmissionStates.REVIEWED,
                    turnInDate = it.turnInDate,
                    projectID = it.projectID,
                    expirationDate = it.expirationDate,
                )
            }

        this.submissionRepository.save(updatedSubmission)

        return updatedSubmission.toSumbissionInfoDTO()
    }

    fun getProjectIdOfUser(email: String): Long {
        val submission =
            this.submissionRepository.findByUserEmail(email)
                ?: throw ResourceNotFoundException("Submission for user with email $email was not found.")
        return submission.projectID
    }
}
