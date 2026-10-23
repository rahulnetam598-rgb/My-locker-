package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.PickedFileInfo
import com.example.model.DocumentCategory
import com.example.ui.viewmodel.DocumentSortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string resource matches MyLocker`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MyLocker", appName)
    }

    @Test
    fun `verify document categories available`() {
        val identity = DocumentCategory.fromId("IDENTITY")
        val education = DocumentCategory.fromId("EDUCATION")
        val other = DocumentCategory.fromId("OTHER")

        assertEquals("Identity", identity.title)
        assertEquals("Education", education.title)
        assertEquals("Other", other.title)
    }

    @Test
    fun `verify picked file info formatting and default title`() {
        val pdfInfo = PickedFileInfo(
            fileName = "college_degree_scan.pdf",
            fileSize = 1048576L,
            mimeType = "application/pdf",
            defaultTitle = "College Degree Scan",
            isPdf = true
        )

        assertEquals("1.00 MB", pdfInfo.formattedSize)
        assertTrue(pdfInfo.isPdf)
        assertEquals("College Degree Scan", pdfInfo.defaultTitle)

        val imageInfo = PickedFileInfo(
            fileName = "passport_photo.png",
            fileSize = 51200L,
            mimeType = "image/png",
            defaultTitle = "Passport Photo",
            isPdf = false
        )
        assertEquals("50.0 KB", imageInfo.formattedSize)
        assertFalse(imageInfo.isPdf)
    }

    @Test
    fun `verify sort orders available`() {
        assertEquals(4, DocumentSortOrder.entries.size)
        assertEquals("Newest First", DocumentSortOrder.DATE_DESC.displayName)
    }
}
