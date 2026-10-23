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
            val contentResolver = context.contentResolver
            var resolvedFileName = "doc_${System.currentTimeMillis()}"
            var resolvedSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { resolvedFileName = it }
                    }
                    if (sizeIndex != -1) {
                        resolvedSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            var mimeType = contentResolver.getType(uri) ?: ""
            if (mimeType.isBlank()) {
                val lowerName = resolvedFileName.lowercase()
                mimeType = when {
                    lowerName.endsWith(".pdf") -> "application/pdf"
                    lowerName.endsWith(".png") -> "image/png"
                    lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
                    lowerName.endsWith(".webp") -> "image/webp"
                    else -> "application/octet-stream"
                }
            }

            val docsDir = File(context.filesDir, "documents")
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            // Create target file in internal storage
            val ext = resolvedFileName.substringAfterLast('.', "")
            val safeName = "file_${System.currentTimeMillis()}${if (ext.isNotEmpty()) ".$ext" else ""}"
            val destinationFile = File(docsDir, safeName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(Exception("Unable to read selected file"))

            val finalSize = if (resolvedSize > 0) resolvedSize else destinationFile.length()
            val finalTitle = customTitle.ifBlank {
                resolvedFileName.substringBeforeLast('.')
            }

            val entity = DocumentEntity(
                title = finalTitle,
                fileName = resolvedFileName,
                filePath = destinationFile.absolutePath,
                mimeType = mimeType,
                fileSize = finalSize,
                category = category,
                documentNumber = docNumber.trim(),
                notes = notes.trim(),
                createdAt = System.currentTimeMillis(),
                isSample = false
            )

            val id = documentDao.insertDocument(entity)
            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
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
