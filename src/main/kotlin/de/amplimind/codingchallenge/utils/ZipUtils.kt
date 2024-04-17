package de.amplimind.codingchallenge.utils

import de.amplimind.codingchallenge.exceptions.ZipBombException
import org.springframework.web.multipart.MultipartFile
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipUtils {
    private const val MAX_FILE_SIZE: Long = 100 * 1024 * 1024 // 100MB

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
