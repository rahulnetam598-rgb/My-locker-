package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.util.SampleDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class PickedFileInfo(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val defaultTitle: String,
    val isPdf: Boolean
) {
    val formattedSize: String
        get() = when {
            fileSize <= 0 -> "Unknown size"
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> String.format("%.1f KB", fileSize / 1024.0)
            else -> String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
        }
}

class DocumentRepository(
    private val context: Context,
    private val documentDao: DocumentDao
) {

    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentsByCategory(category: String): Flow<List<DocumentEntity>> {
        return documentDao.getDocumentsByCategory(category)
    }

    suspend fun getDocumentById(id: Long): DocumentEntity? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)
    }

    suspend fun seedInitialSamplesIfEmpty() = withContext(Dispatchers.IO) {
        val count = documentDao.getDocumentCount()
        if (count == 0) {
            val samples = SampleDataGenerator.generateSampleDocuments(context)
            for (sample in samples) {
                documentDao.insertDocument(sample)
            }
        }
    }

    suspend fun restoreSampleDocuments() = withContext(Dispatchers.IO) {
        val samples = SampleDataGenerator.generateSampleDocuments(context)
        for (sample in samples) {
            documentDao.insertDocument(sample)
        }
    }

    suspend fun clearSampleDocuments(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val samples = documentDao.getSampleDocuments()
            for (s in samples) {
                val f = File(s.filePath)
                if (f.exists()) f.delete()
            }
            documentDao.deleteSampleDocuments()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inspects a picked URI to extract its filename, size, MIME type, and default clean title
     * so that AddDocumentDialog can immediately present user-friendly details.
     */
    fun inspectPickedFile(uri: Uri): PickedFileInfo {
        var fileName = "Document_${System.currentTimeMillis()}"
        var fileSize = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { if (it.isNotBlank()) fileName = it }
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var mimeType = try {
            context.contentResolver.getType(uri) ?: ""
        } catch (e: Exception) {
            ""
        }

        val lowerName = fileName.lowercase()
        if (mimeType.isBlank()) {
            mimeType = when {
                lowerName.endsWith(".pdf") -> "application/pdf"
                lowerName.endsWith(".png") -> "image/png"
                lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
                lowerName.endsWith(".webp") -> "image/webp"
                else -> "application/octet-stream"
            }
        }

        val isPdf = mimeType.equals("application/pdf", ignoreCase = true) || lowerName.endsWith(".pdf")

        // Clean default title from file name
        val rawBase = fileName.substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()

        val defaultTitle = if (rawBase.isNotBlank()) {
            rawBase.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            if (isPdf) "My PDF Document" else "My Document"
        }

        return PickedFileInfo(
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType,
            defaultTitle = defaultTitle,
            isPdf = isPdf
        )
    }

    /**
     * Imports a document selected via the Android system file picker
     * and saves it to internal app storage (private to MyLocker, no server upload).
     */
    suspend fun importDocument(
        uri: Uri,
        customTitle: String,
        category: String,
        docNumber: String,
        notes: String
    ): Result<DocumentEntity> = withContext(Dispatchers.IO) {
        try {
            val fileInfo = inspectPickedFile(uri)
            val docsDir = File(context.filesDir, "documents")
            if (!docsDir.exists()) {
                val created = docsDir.mkdirs()
                if (!created && !docsDir.exists()) {
                    return@withContext Result.failure(IOException("Failed to create local documents directory on device"))
                }
            }

            // Create target file in internal storage
            val ext = fileInfo.fileName.substringAfterLast('.', "")
            val safeName = "file_${System.currentTimeMillis()}${if (ext.isNotEmpty()) ".$ext" else ""}"
            val destinationFile = File(docsDir, safeName)

            val bytesCopied = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IOException("Unable to access the selected file. Please verify file permissions."))

            if (bytesCopied == 0L && destinationFile.length() == 0L) {
                destinationFile.delete()
                return@withContext Result.failure(IOException("Selected file appears to be empty or unreadable."))
            }

            val finalSize = if (fileInfo.fileSize > 0) fileInfo.fileSize else destinationFile.length()
            val finalTitle = customTitle.ifBlank { fileInfo.defaultTitle }

            val entity = DocumentEntity(
                title = finalTitle,
                fileName = fileInfo.fileName,
                filePath = destinationFile.absolutePath,
                mimeType = fileInfo.mimeType,
                fileSize = finalSize,
                category = category,
                documentNumber = docNumber.trim(),
                notes = notes.trim(),
                createdAt = System.currentTimeMillis(),
                isSample = false
            )

            val id = documentDao.insertDocument(entity)
            Result.success(entity.copy(id = id))
        } catch (e: SecurityException) {
            Result.failure(Exception("Permission denied accessing file: ${e.localizedMessage}"))
        } catch (e: IOException) {
            Result.failure(Exception("Storage error while saving document: ${e.localizedMessage}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Unexpected error importing document: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    suspend fun updateDocumentDetails(
        id: Long,
        newTitle: String,
        newCategory: String,
        newDocNumber: String,
        newNotes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = documentDao.getDocumentById(id)
                ?: return@withContext Result.failure(Exception("Document not found"))
            val updated = existing.copy(
                title = newTitle.trim(),
                category = newCategory,
                documentNumber = newDocNumber.trim(),
                notes = newNotes.trim()
            )
            documentDao.updateDocument(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(document: DocumentEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            documentDao.deleteDocument(document)
            // Delete local file if it exists in internal storage
            val file = File(document.filePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
