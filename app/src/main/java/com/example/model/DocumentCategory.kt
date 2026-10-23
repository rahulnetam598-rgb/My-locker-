package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryEducationTeal
import com.example.ui.theme.CategoryIdentityBlue
import com.example.ui.theme.CategoryOtherIndigo

enum class DocumentCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    IDENTITY(
        id = "IDENTITY",
        title = "Identity",
        description = "ID cards, passports, driver licenses, badges",
        icon = Icons.Default.Badge,
        accentColor = CategoryIdentityBlue
    ),
    EDUCATION(
        id = "EDUCATION",
        title = "Education",
        description = "Diplomas, transcripts, certificates, course records",
        icon = Icons.Default.School,
        accentColor = CategoryEducationTeal
    ),
    OTHER(
        id = "OTHER",
        title = "Other",
        description = "Medical cards, invoices, warranties, contracts",
        icon = Icons.Default.Folder,
        accentColor = CategoryOtherIndigo
    );

    companion object {
        fun fromId(id: String): DocumentCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }
    }
}
