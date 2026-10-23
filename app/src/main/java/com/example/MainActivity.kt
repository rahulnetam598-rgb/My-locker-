package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddDocumentDialog
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.dialogs.DisclaimerDialog
import com.example.ui.dialogs.EditDocumentDialog
import com.example.ui.dialogs.SecuritySettingsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentPreviewScreen
import com.example.ui.screens.LockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LockerViewModel
import com.example.util.PdfUtils
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: LockerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LockerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LockerApp(viewModel: LockerViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 1. App Lock Screen Barrier (Requirement 7)
    if (uiState.isLockActive && !uiState.isAppUnlocked) {
        LockScreen(
            pinInput = uiState.pinInput,
            errorMessage = uiState.pinErrorMessage,
            onDigitEntered = { digit -> viewModel.onPinDigitEntered(digit) },
            onBackspace = { viewModel.onPinBackspace() },
            onSkipLock = { viewModel.skipLockForTesting() }
        )
        return
    }

    // 2. Main Navigation Flow: Preview vs Dashboard
    val previewDoc = uiState.previewDocument
    if (previewDoc != null) {
        DocumentPreviewScreen(
            document = previewDoc,
            onBack = { viewModel.onSelectDocumentForPreview(null) },
            onEdit = { viewModel.onInitiateEdit(previewDoc) },
            onDelete = { viewModel.onInitiateDelete(previewDoc) },
            onShare = {
                PdfUtils.shareDocument(
                    context = context,
                    file = File(previewDoc.filePath),
                    mimeType = previewDoc.mimeType,
                    title = previewDoc.title
                )
            },
            onOpenExternal = {
                PdfUtils.openDocumentExternally(
                    context = context,
                    file = File(previewDoc.filePath),
                    mimeType = previewDoc.mimeType
                )
            }
        )
    } else {
        DashboardScreen(
            viewModel = viewModel,
            uiState = uiState
        )
    }

    // 3. Dialog Overlays
    // Add Document Dialog
    if (uiState.isAddDialogOpen && uiState.pendingImportUri != null) {
        AddDocumentDialog(
            uri = uiState.pendingImportUri!!,
            fileInfo = uiState.pendingFileInfo,
            onDismiss = { viewModel.dismissAddDialog() },
            onSave = { title, category, docNumber, notes ->
                viewModel.confirmAddDocument(title, category, docNumber, notes)
            }
        )
    }

    // Edit / Rename Document Dialog
    uiState.documentToEdit?.let { doc ->
        EditDocumentDialog(
            document = doc,
            onDismiss = { viewModel.dismissEditDialog() },
            onSave = { newTitle, newCategory, newDocNumber, newNotes ->
                viewModel.confirmEditDocument(newTitle, newCategory, newDocNumber, newNotes)
            }
        )
    }

    // Delete Confirmation Dialog
    uiState.documentToDelete?.let { doc ->
        DeleteConfirmDialog(
            document = doc,
            onDismiss = { viewModel.dismissDeleteDialog() },
            onConfirm = { viewModel.confirmDeleteDocument() }
        )
    }

    // Security & App Lock Settings Dialog
    if (uiState.isSecurityDialogOpen) {
        SecuritySettingsDialog(
            isLockEnabled = uiState.isLockActive,
            currentPin = viewModel.securityManager.getSavedPin(),
            onDismiss = { viewModel.dismissSecurityDialog() },
            onToggleLock = { enabled -> viewModel.onToggleAppLock(enabled) },
            onChangePin = { newPin -> viewModel.onChangePin(newPin) }
        )
    }

    // Non-Affiliation and Local Storage Disclaimer Dialog
    if (uiState.isDisclaimerDialogOpen) {
        DisclaimerDialog(
            onDismiss = { viewModel.dismissDisclaimerDialog() }
        )
    }
}
