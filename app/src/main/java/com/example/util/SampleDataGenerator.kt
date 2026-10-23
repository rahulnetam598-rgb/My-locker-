package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.data.local.DocumentEntity
import java.io.File
import java.io.FileOutputStream

object SampleDataGenerator {

    fun generateSampleDocuments(context: Context): List<DocumentEntity> {
        val docsDir = File(context.filesDir, "documents")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }

        val samples = mutableListOf<DocumentEntity>()

        // 1. Identity Sample
        val idFile = File(docsDir, "sample_identity_card.png")
        if (!idFile.exists()) {
            createSampleCard(
                file = idFile,
                headerTitle = "NATIONAL IDENTITY SPECIMEN",
                cardHolder = "Alex Morgan",
                idNumber = "ID-9081-SAMPLE",
                categoryText = "IDENTITY (SAMPLE)",
                accentColorHex = 0xFF0B57D0.toInt()
            )
        }
        samples.add(
            DocumentEntity(
                title = "Sample National Identity Card",
                fileName = "sample_identity_card.png",
                filePath = idFile.absolutePath,
                mimeType = "image/png",
                fileSize = idFile.length(),
                category = "IDENTITY",
                documentNumber = "ID-9081-SAMPLE",
                notes = "Sample identity card specimen for preview testing. Not an official credential.",
                createdAt = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
                isSample = true
            )
        )

        // 2. Education Sample
        val eduFile = File(docsDir, "sample_education_degree.png")
        if (!eduFile.exists()) {
            createSampleCard(
                file = eduFile,
                headerTitle = "UNIVERSITY DEGREE SPECIMEN",
                cardHolder = "Alex Morgan",
                idNumber = "DEG-2024-TEST",
                categoryText = "EDUCATION (SAMPLE)",
                accentColorHex = 0xFF0284C7.toInt()
            )
        }
        samples.add(
            DocumentEntity(
                title = "Sample University Degree Certificate",
                fileName = "sample_education_degree.png",
                filePath = eduFile.absolutePath,
                mimeType = "image/png",
                fileSize = eduFile.length(),
                category = "EDUCATION",
                documentNumber = "DEG-2024-TEST",
                notes = "Bachelor of Science in Computer Engineering specimen certificate.",
                createdAt = System.currentTimeMillis() - 86400000L * 5, // 5 days ago
                isSample = true
            )
        )

        // 3. Other Sample
        val otherFile = File(docsDir, "sample_insurance_card.png")
        if (!otherFile.exists()) {
            createSampleCard(
                file = otherFile,
                headerTitle = "HEALTHCARE POLICY SPECIMEN",
                cardHolder = "Alex Morgan",
                idNumber = "MED-POL-7721",
                categoryText = "OTHER (SAMPLE)",
                accentColorHex = 0xFF4338CA.toInt()
            )
        }
        samples.add(
            DocumentEntity(
                title = "Sample Health Insurance Policy",
                fileName = "sample_insurance_card.png",
                filePath = otherFile.absolutePath,
                mimeType = "image/png",
                fileSize = otherFile.length(),
                category = "OTHER",
                documentNumber = "MED-POL-7721",
                notes = "Sample personal insurance card placeholder for local wallet testing.",
                createdAt = System.currentTimeMillis() - 86400000L * 10,
                isSample = true
            )
        )

        return samples
    }

    private fun createSampleCard(
        file: File,
        headerTitle: String,
        cardHolder: String,
        idNumber: String,
        categoryText: String,
        accentColorHex: Int
    ) {
        val width = 1000
        val height = 620
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Top decorative accent banner
        paint.color = accentColorHex
        canvas.drawRect(0f, 0f, width.toFloat(), 120f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 38f
        paint.isFakeBoldText = true
        canvas.drawText(headerTitle, 50f, 75f, paint)

        // Sample Watermark Warning Tag (Red)
        paint.color = 0xFFDC2626.toInt()
        val badgeRect = RectF(50f, 140f, 620f, 195f)
        canvas.drawRoundRect(badgeRect, 12f, 12f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("[SAMPLE DOCUMENT - FOR TESTING ONLY]", 65f, 178f, paint)

        // Photo / Chip Placeholder box
        paint.color = 0xFFF1F5F9.toInt()
        val photoRect = RectF(50f, 220f, 250f, 480f)
        canvas.drawRoundRect(photoRect, 16f, 16f, paint)

        paint.color = 0xFF94A3B8.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(photoRect, 16f, 16f, paint)
        paint.style = Paint.Style.FILL

        paint.color = 0xFF64748B.toInt()
        paint.textSize = 28f
        canvas.drawText("PHOTO", 95f, 350f, paint)
        paint.textSize = 20f
        canvas.drawText("SPECIMEN", 80f, 385f, paint)

        // Cardholder details
        paint.color = 0xFF64748B.toInt()
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("DOCUMENT HOLDER", 280f, 250f, paint)

        paint.color = 0xFF0F172A.toInt()
        paint.textSize = 38f
        paint.isFakeBoldText = true
        canvas.drawText(cardHolder, 280f, 295f, paint)

        paint.color = 0xFF64748B.toInt()
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("RECORD NUMBER", 280f, 355f, paint)

        paint.color = accentColorHex
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText(idNumber, 280f, 395f, paint)

        paint.color = 0xFF64748B.toInt()
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("CATEGORY", 280f, 450f, paint)

        paint.color = 0xFF0F172A.toInt()
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText(categoryText, 280f, 485f, paint)

        // Bottom Disclaimer
        paint.color = 0xFFE2E8F0.toInt()
        canvas.drawRect(0f, height - 80f, width.toFloat(), height.toFloat(), paint)

        paint.color = 0xFF475569.toInt()
        paint.textSize = 20f
        paint.isFakeBoldText = false
        canvas.drawText("MYLOCKER DIGITAL WALLET • SAVED LOCALLY ON DEVICE • NOT AN OFFICIAL ID", 50f, height - 35f, paint)

        // Save to file
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
