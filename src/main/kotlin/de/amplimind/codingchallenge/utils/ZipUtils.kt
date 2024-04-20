package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.ZipBombException
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object ZipUtils {
    private const val MAX_FILE_SIZE: Long = 100 * 1024 * 1024 // 100MB

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

        while (entry != null) {
            val buffer = ByteArray(1024)
            var len: Int
            val content = StringBuilder()
            while (zipInputStream.read(buffer).also { len = it } > 0) {
                content.append(String(buffer, 0, len))
            }
            println("Content:\n$content\n")
            if (!entry.isDirectory) {
                files[entry.name] = Base64.getEncoder().encodeToString(zipInputStream.readBytes())
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
    /**
     * checks for zip bombs by parsing the files input stream. For that reason nested zip
     * files are not allowed. If a nested zip is found or the file size are exceeded immediately
     * stops parsing and throws an error!
     *
     * @param zipFile the zip file as a multipart file gained from the endpoint
     *
     * @throws ZipBombException if a potential zip bomb is found throws this exception
     */
    @Throws(ZipBombException::class)
    fun checkZip(zipFile: MultipartFile) {
        var size = 0L
        val zipInputStream = ZipInputStream(zipFile.inputStream)
        zipInputStream.use { zis ->
            var zipEntry: ZipEntry?
            while (zis.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry!!.isDirectory) {
                    // if the size of the directory would be counted the directors size is counted twice
                    continue
                }
                if (zipEntry!!.name.endsWith(".zip")) {
                    // nested zips are not allowed
                    throw ZipBombException("Potential zip bomb detected: Nested zip files are not allowed!")
                }

                size += zipEntry!!.size
                if (size > MAX_FILE_SIZE) {
                    // file size is exceeded
                    throw ZipBombException("Potential zip bomb detected: File size exceeds max file size")
                }
            }
        }
    }
}
