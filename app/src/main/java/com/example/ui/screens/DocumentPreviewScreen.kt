package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.DocumentEntity
import com.example.model.DocumentCategory
import com.example.ui.theme.LockerBluePrimary
import com.example.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentPreviewScreen(
    document: DocumentEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val file = remember(document.filePath) { File(document.filePath) }
    val category = DocumentCategory.fromId(document.category)
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(document.createdAt) { dateFormat.format(Date(document.createdAt)) }

    // PDF state
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfPageCount by remember { mutableIntStateOf(0) }
    var currentPdfPage by remember { mutableIntStateOf(0) }
    var isLoadingPdf by remember { mutableStateOf(false) }
    var pdfLoadError by remember { mutableStateOf<String?>(null) }

    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(document.id, currentPdfPage) {
        if (document.isPdf) {
            if (file.exists()) {
                isLoadingPdf = true
                pdfLoadError = null
                withContext(Dispatchers.IO) {
                    try {
                        pdfPageCount = PdfUtils.getPdfPageCount(file)
                        val bitmap = PdfUtils.renderPdfPage(context, file, currentPdfPage)
                        if (bitmap != null) {
                            pdfBitmap = bitmap
                        } else {
                            pdfLoadError = "Unable to render PDF preview. You can still open it with your system viewer."
                        }
                    } catch (e: Exception) {
                        pdfLoadError = "Error loading PDF: ${e.localizedMessage}"
                    }
                }
                isLoadingPdf = false
            } else {
                pdfLoadError = "PDF file not found in local storage."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD))
            .statusBarsPadding()
            .testTag("document_preview_screen")
    ) {
        // Preview Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("preview_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
                Text(
                    text = "${category.title} • ${document.formattedSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier.testTag("preview_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = LockerBluePrimary
                )
            }

            IconButton(
                onClick = onOpenExternal,
                modifier = Modifier.testTag("preview_open_external_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open in External App",
                    tint = LockerBluePrimary
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.widthIn(max = 680.dp)) {
                Column {
                    // Check if physical file is missing
                    if (!file.exists()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "File Missing from Local Storage",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB91C1C)
                                        )
                                    )
                                    Text(
                                        text = "The file at '${document.filePath}' is no longer present on device.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                                    )
                                }
                            }
                        }
                    }

                    // Sample Warning Banner
                    if (document.isSample) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SAMPLE DOCUMENT (TEST SPECIMEN)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                    Text(
                                        text = "This is a placeholder specimen generated strictly for testing. It carries no legal or official standing.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F)
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Visual Document Preview Frame with Pinch-To-Zoom & Pan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_visual_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Zoom / Reset Controls Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (document.isPdf) "PDF Page ${currentPdfPage + 1} of ${maxOf(1, pdfPageCount)}" else "Document Image",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            scale = maxOf(1f, scale - 0.5f)
                                            if (scale == 1f) {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        },
                                        enabled = scale > 1f,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                                    }

                                    Text(
                                        text = "${(scale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF64748B)
                                    )

                                    IconButton(
                                        onClick = {
                                            scale = minOf(4f, scale + 0.5f)
                                        },
                                        enabled = scale < 4f,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                                    }

                                    if (scale != 1f || offsetX != 0f || offsetY != 0f) {
                                        IconButton(
                                            onClick = {
                                                scale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            // Image or PDF Viewport with gesture transform
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 4f)
                                            if (scale > 1f) {
                                                val maxOffset = (scale - 1f) * 300f
                                                offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                                offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                                            } else {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (document.isPdf) {
                                    if (isLoadingPdf) {
                                        CircularProgressIndicator(color = LockerBluePrimary)
                                    } else if (pdfBitmap != null) {
                                        Image(
                                            bitmap = pdfBitmap!!.asImageBitmap(),
                                            contentDescription = "PDF Page ${currentPdfPage + 1}",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = scale,
                                                    scaleY = scale,
                                                    translationX = offsetX,
                                                    translationY = offsetY
                                                ),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "PDF Document",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = pdfLoadError ?: "Tap below to view full document in external PDF reader",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                } else {
                                    // Image
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(file)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = document.title,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = scale,
                                                scaleY = scale,
                                                translationX = offsetX,
                                                translationY = offsetY
                                            ),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            // PDF Page Switcher Controls
                            if (document.isPdf && pdfPageCount > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (currentPdfPage > 0) {
                                                currentPdfPage--
                                                scale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        },
                                        enabled = currentPdfPage > 0
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                                    }
                                    Text(
                                        text = "Page ${currentPdfPage + 1} of $pdfPageCount",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (currentPdfPage < pdfPageCount - 1) {
                                                currentPdfPage++
                                                scale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        },
                                        enabled = currentPdfPage < pdfPageCount - 1
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metadata Detail Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DOCUMENT INFORMATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow(label = "Category", value = category.title)
                            DetailRow(label = "Document ID", value = document.documentNumber.ifBlank { "Not specified" })
                            DetailRow(label = "File Name", value = document.fileName)
                            DetailRow(label = "File Size", value = document.formattedSize)
                            DetailRow(label = "MIME Type", value = document.mimeType)
                            DetailRow(label = "Added On", value = formattedDate)

                            if (document.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = document.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Storage Location: Local Device Internal Storage (No cloud sync)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenExternal,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_open_external_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = LockerBluePrimary)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("System Viewer")
                        }

                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rename / Edit")
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_delete_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF0F172A)
        )
    }
}
