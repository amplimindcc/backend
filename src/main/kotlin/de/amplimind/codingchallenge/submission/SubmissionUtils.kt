package de.amplimind.codingchallenge.submission

import com.fasterxml.jackson.databind.ser.Serializers.Base
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.ZipInputStream

object SubmissionUtils {
    private val logger = LoggerFactory.getLogger(javaClass)


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
                val buffer = ByteArray(1024)
                var len: Int
                val outputStream = ByteArrayOutputStream()
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                files[entry.name] = Base64.getEncoder().encodeToString(outputStream.toByteArray())
            } else {
                traverseFolder(zipInputStream, files)
            }
            entry = zipInputStream.nextEntry
        }
        return files
    }

    private fun traverseFolder2(zipInputStream: ZipInputStream, files: MutableMap<String, String>): MutableMap<String, String> {
        var entry = zipInputStream.nextEntry

        // Loop through each entry in the zip file
        while (entry != null) {
            // Print the name of the entry
//            println("File Name: ${entry.name}")

            // Read and print the content of the entry
            val buffer = ByteArray(1024)
            var len: Int
            val content = StringBuilder()
            while (zipInputStream.read(buffer).also { len = it } > 0) {
                content.append(String(buffer, 0, len))
            }
            println("Content:\n$content\n")
            if (!entry.isDirectory) {
                files[entry.name] = Base64.getEncoder().encodeToString(zipInputStream.readBytes())
//                logger.info(files[entry.name])
            }

            // Move to the next entry
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