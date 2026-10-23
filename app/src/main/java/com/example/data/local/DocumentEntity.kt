package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val fileSize: Long,
    val category: String, // IDENTITY, EDUCATION, OTHER
    val documentNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
) {
    val isPdf: Boolean
        get() = mimeType.equals("application/pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true)

    val formattedSize: String
        get() {
            return when {
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> String.format("%.1f KB", fileSize / 1024.0)
                else -> String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
            }
        }
}
