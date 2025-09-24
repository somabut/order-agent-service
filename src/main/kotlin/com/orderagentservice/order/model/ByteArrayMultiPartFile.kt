package com.orderagentservice.order.model

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ByteArrayMultiPartFile(
    private val fileContent: ByteArray,
    private val fileName: String,
    private val contentType: String = "application/octet-stream"
) : MultipartFile {
    override fun getName(): String = "file"
    override fun getOriginalFilename(): String = fileName
    override fun getContentType(): String = contentType
    override fun isEmpty(): Boolean = fileContent.isEmpty()
    override fun getSize(): Long = fileContent.size.toLong()
    override fun getBytes(): ByteArray = fileContent
    override fun getInputStream(): InputStream = ByteArrayInputStream(fileContent)
    override fun transferTo(dest: File) {
        FileOutputStream(dest).use { it.write(fileContent) }
    }
}