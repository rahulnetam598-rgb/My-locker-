package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DocumentCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
