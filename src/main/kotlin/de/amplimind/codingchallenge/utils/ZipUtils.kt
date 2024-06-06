package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.ForbiddenFileNameException
import de.amplimind.codingchallenge.exceptions.LinterResultNotAvailableException
import de.amplimind.codingchallenge.exceptions.UnzipException
import de.amplimind.codingchallenge.exceptions.ZipBombException
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64
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
        checkZip(zipFileContent)
        val files = mutableMapOf<String, String>()
        try {
            ZipInputStream(zipFileContent.inputStream).use { zipInputStream ->
                traverseFolder(zipInputStream, files)
            }
        } catch (e: Exception) {
            throw UnzipException("Error while unzipping the file")
        }
        return files
    }

    /**
     * recursively traverse through the zip folder
     * @param zipInputStream the zipfile
     * @param files a map of the files and their paths
     * @return the [Map] of the files and their content base64 encoded
     */
    private fun traverseFolder(
        zipInputStream: ZipInputStream,
        files: MutableMap<String, String>,
    ): MutableMap<String, String> {
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
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

                if (zipEntry!!.name.equals("README.md")) {
                    throw ForbiddenFileNameException("No README.md file is allowed to be in the directory root!")
                }

                size += zipEntry!!.size
                if (size > MAX_FILE_SIZE) {
                    // file size is exceeded
                    throw ZipBombException("Potential zip bomb detected: File size exceeds max file size")
                }
            }
        }
    }

    /**
     * Open a zip file from a byte stream
     * @param byteStream the byte stream of the zip file
     * @return the [ZipInputStream] of the zip file
     */
    fun openZipFile(byteStream: InputStream): ZipInputStream {
        return ZipInputStream(byteStream)
    }

    /**
     * Read the content of the megalinter.log file from the zip file
     * @param zipFile the zip file
     * @return the content of the megalinter.log file
     */
    fun readLintResult(zipFile: ZipInputStream): String {
        var entry = zipFile.nextEntry
        while (entry != null) {
            if (entry.name == "megalinter-reports/megalinter.log") {
                val buffer = ByteArray(1024)
                val outputStream = ByteArrayOutputStream()
                var len: Int
                while (zipFile.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                return outputStream.toString()
            }
            entry = zipFile.nextEntry
        }
        throw LinterResultNotAvailableException("Error while reading the linter result: megalinter.log not found")
    }
}
