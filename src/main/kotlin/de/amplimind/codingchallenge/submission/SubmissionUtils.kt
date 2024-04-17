package de.amplimind.codingchallenge.submission

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.ZipInputStream

object SubmissionUtils {

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
    private fun traverseFolder(zipInputStream: ZipInputStream, files: MutableMap<String, String>): MutableMap<String, String> {
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
}