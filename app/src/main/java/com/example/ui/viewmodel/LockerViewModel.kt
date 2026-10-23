package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.local.MyLockerDatabase
import com.example.data.preferences.LockerSecurityManager
import com.example.data.repository.DocumentRepository
import com.example.data.repository.PickedFileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DocumentSortOrder(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    TITLE_ASC("Name (A–Z)"),
    SIZE_DESC("Largest First")
}

data class LockerUiState(
    val documents: List<DocumentEntity> = emptyList(),
    val filteredDocuments: List<DocumentEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null, // null means All
    val sortOrder: DocumentSortOrder = DocumentSortOrder.DATE_DESC,
    val showSamplesOnly: Boolean? = null, // null: all, true: only samples, false: hide samples
    val totalCount: Int = 0,
    val identityCount: Int = 0,
    val educationCount: Int = 0,
    val otherCount: Int = 0,
    val sampleCount: Int = 0,
    val totalStorageBytes: Long = 0,
    val previewDocument: DocumentEntity? = null,
    val documentToEdit: DocumentEntity? = null,
    val documentToDelete: DocumentEntity? = null,
    val pendingImportUri: Uri? = null,
    val pendingFileInfo: PickedFileInfo? = null,
    val isAddDialogOpen: Boolean = false,
    val isSecurityDialogOpen: Boolean = false,
    val isDisclaimerDialogOpen: Boolean = false,
    val userNotification: String? = null,
    val errorMessage: String? = null,
    val isLockActive: Boolean = false,
    val isAppUnlocked: Boolean = true,
    val pinInput: String = "",
    val pinErrorMessage: String? = null
)

class LockerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MyLockerDatabase.getInstance(application)
    val repository = DocumentRepository(application, database.documentDao())
    val securityManager = LockerSecurityManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(DocumentSortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    private val _showSamplesOnly = MutableStateFlow<Boolean?>(null)
    val showSamplesOnly = _showSamplesOnly.asStateFlow()

    private val _previewDocument = MutableStateFlow<DocumentEntity?>(null)
    val previewDocument = _previewDocument.asStateFlow()

    private val _documentToEdit = MutableStateFlow<DocumentEntity?>(null)
    val documentToEdit = _documentToEdit.asStateFlow()

    private val _documentToDelete = MutableStateFlow<DocumentEntity?>(null)
    val documentToDelete = _documentToDelete.asStateFlow()

    private val _pendingImportUri = MutableStateFlow<Uri?>(null)
    val pendingImportUri = _pendingImportUri.asStateFlow()

    private val _pendingFileInfo = MutableStateFlow<PickedFileInfo?>(null)
    val pendingFileInfo = _pendingFileInfo.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen = _isAddDialogOpen.asStateFlow()

    private val _isSecurityDialogOpen = MutableStateFlow(false)
    val isSecurityDialogOpen = _isSecurityDialogOpen.asStateFlow()

    private val _isDisclaimerDialogOpen = MutableStateFlow(false)
    val isDisclaimerDialogOpen = _isDisclaimerDialogOpen.asStateFlow()

    private val _userNotification = MutableStateFlow<String?>(null)
    val userNotification = _userNotification.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput = _pinInput.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage = _pinErrorMessage.asStateFlow()

    private val _isLockActiveState = MutableStateFlow(securityManager.isLockEnabled())

    val uiState: StateFlow<LockerUiState> = combine(
        repository.allDocuments,
        _searchQuery,
        _selectedCategoryFilter,
        _sortOrder,
        _showSamplesOnly,
        _previewDocument,
        _documentToEdit,
        _documentToDelete,
        _pendingImportUri,
        _pendingFileInfo,
        _isAddDialogOpen,
        _isSecurityDialogOpen,
        _isDisclaimerDialogOpen,
        _userNotification,
        _errorMessage,
        _isLockActiveState,
        securityManager.isUnlocked,
        _pinInput,
        _pinErrorMessage
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val docs = args[0] as List<DocumentEntity>
        val query = args[1] as String
        val catFilter = args[2] as String?
        val sort = args[3] as DocumentSortOrder
        val sampleFilter = args[4] as Boolean?
        val preview = args[5] as DocumentEntity?
        val toEdit = args[6] as DocumentEntity?
        val toDelete = args[7] as DocumentEntity?
        val pendingUri = args[8] as Uri?
        val fileInfo = args[9] as PickedFileInfo?
        val addOpen = args[10] as Boolean
        val secOpen = args[11] as Boolean
        val discOpen = args[12] as Boolean
        val notif = args[13] as String?
        val errMsg = args[14] as String?
        val lockActive = args[15] as Boolean
        val unlocked = args[16] as Boolean
        val pin = args[17] as String
        val pinErr = args[18] as String?

        var filtered = docs.filter { doc ->
            val matchesCategory = catFilter == null || doc.category.equals(catFilter, ignoreCase = true)
            val matchesSamples = when (sampleFilter) {
                true -> doc.isSample
                false -> !doc.isSample
                null -> true
            }
            val matchesQuery = query.isBlank() ||
                doc.title.contains(query, ignoreCase = true) ||
                doc.documentNumber.contains(query, ignoreCase = true) ||
                doc.notes.contains(query, ignoreCase = true) ||
                doc.category.contains(query, ignoreCase = true)
            matchesCategory && matchesSamples && matchesQuery
        }

        filtered = when (sort) {
            DocumentSortOrder.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
            DocumentSortOrder.DATE_ASC -> filtered.sortedBy { it.createdAt }
            DocumentSortOrder.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            DocumentSortOrder.SIZE_DESC -> filtered.sortedByDescending { it.fileSize }
        }

        val idCount = docs.count { it.category.equals("IDENTITY", ignoreCase = true) }
        val eduCount = docs.count { it.category.equals("EDUCATION", ignoreCase = true) }
        val otherCount = docs.count { it.category.equals("OTHER", ignoreCase = true) }
        val sampleDocsCount = docs.count { it.isSample }
        val totalBytes = docs.sumOf { it.fileSize }

        LockerUiState(
            documents = docs,
            filteredDocuments = filtered,
            searchQuery = query,
            selectedCategoryFilter = catFilter,
            sortOrder = sort,
            showSamplesOnly = sampleFilter,
            totalCount = docs.size,
            identityCount = idCount,
            educationCount = eduCount,
            otherCount = otherCount,
            sampleCount = sampleDocsCount,
            totalStorageBytes = totalBytes,
            previewDocument = preview,
            documentToEdit = toEdit,
            documentToDelete = toDelete,
            pendingImportUri = pendingUri,
            pendingFileInfo = fileInfo,
            isAddDialogOpen = addOpen,
            isSecurityDialogOpen = secOpen,
            isDisclaimerDialogOpen = discOpen,
            userNotification = notif,
            errorMessage = errMsg,
            isLockActive = lockActive,
            isAppUnlocked = unlocked,
            pinInput = pin,
            pinErrorMessage = pinErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LockerUiState(
            isLockActive = securityManager.isLockEnabled(),
            isAppUnlocked = securityManager.isUnlocked.value
        )
    )

    init {
        viewModelScope.launch {
            repository.seedInitialSamplesIfEmpty()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterSelected(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun onSortOrderSelected(order: DocumentSortOrder) {
        _sortOrder.value = order
    }

    fun onToggleSampleFilter(filter: Boolean?) {
        _showSamplesOnly.value = filter
    }

    fun onSelectDocumentForPreview(document: DocumentEntity?) {
        _previewDocument.value = document
    }

    fun onInitiateAddDocument(uri: Uri) {
        _pendingImportUri.value = uri
        _pendingFileInfo.value = repository.inspectPickedFile(uri)
        _isAddDialogOpen.value = true
    }

    fun dismissAddDialog() {
        _pendingImportUri.value = null
        _pendingFileInfo.value = null
        _isAddDialogOpen.value = false
    }

    fun confirmAddDocument(
        title: String,
        category: String,
        docNumber: String,
        notes: String
    ) {
        val uri = _pendingImportUri.value ?: return
        viewModelScope.launch {
            val result = repository.importDocument(uri, title, category, docNumber, notes)
            result.onSuccess {
                _userNotification.value = "Document '${it.title}' added to your locker."
                dismissAddDialog()
            }.onFailure { err ->
                _errorMessage.value = "Failed to add document: ${err.message}"
            }
        }
    }

    fun onInitiateEdit(document: DocumentEntity) {
        _documentToEdit.value = document
    }

    fun dismissEditDialog() {
        _documentToEdit.value = null
    }

    fun confirmEditDocument(
        newTitle: String,
        newCategory: String,
        newDocNumber: String,
        newNotes: String
    ) {
        val doc = _documentToEdit.value ?: return
        viewModelScope.launch {
            val result = repository.updateDocumentDetails(doc.id, newTitle, newCategory, newDocNumber, newNotes)
            result.onSuccess {
                _userNotification.value = "Document updated successfully."
                // Update preview if currently previewing this document
                if (_previewDocument.value?.id == doc.id) {
                    _previewDocument.value = repository.getDocumentById(doc.id)
                }
                dismissEditDialog()
            }.onFailure { err ->
                _errorMessage.value = "Failed to update: ${err.message}"
            }
        }
    }

    fun onInitiateDelete(document: DocumentEntity) {
        _documentToDelete.value = document
    }

    fun dismissDeleteDialog() {
        _documentToDelete.value = null
    }

    fun confirmDeleteDocument() {
        val doc = _documentToDelete.value ?: return
        viewModelScope.launch {
            val result = repository.deleteDocument(doc)
            result.onSuccess {
                _userNotification.value = "'${doc.title}' deleted from device."
                if (_previewDocument.value?.id == doc.id) {
                    _previewDocument.value = null
                }
                dismissDeleteDialog()
            }.onFailure { err ->
                _errorMessage.value = "Failed to delete: ${err.message}"
            }
        }
    }

    fun restoreSampleDocuments() {
        viewModelScope.launch {
            repository.restoreSampleDocuments()
            _userNotification.value = "Sample documents restored for testing."
        }
    }

    fun clearSampleDocuments() {
        viewModelScope.launch {
            val result = repository.clearSampleDocuments()
            result.onSuccess {
                _userNotification.value = "Sample documents removed from locker."
            }.onFailure { err ->
                _errorMessage.value = "Error removing samples: ${err.message}"
            }
        }
    }

    fun openSecurityDialog() {
        _isSecurityDialogOpen.value = true
    }

    fun dismissSecurityDialog() {
        _isSecurityDialogOpen.value = false
    }

    fun openDisclaimerDialog() {
        _isDisclaimerDialogOpen.value = true
    }

    fun dismissDisclaimerDialog() {
        _isDisclaimerDialogOpen.value = false
    }

    fun clearNotification() {
        _userNotification.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // App Lock Methods
    fun onPinDigitEntered(digit: String) {
        if (_pinInput.value.length < 4) {
            val newPin = _pinInput.value + digit
            _pinInput.value = newPin
            _pinErrorMessage.value = null
            if (newPin.length == 4) {
                verifyPin(newPin)
            }
        }
    }

    fun onPinBackspace() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinErrorMessage.value = null
        }
    }

    private fun verifyPin(pin: String) {
        if (securityManager.verifyPin(pin)) {
            _pinInput.value = ""
            _pinErrorMessage.value = null
        } else {
            _pinErrorMessage.value = "Incorrect PIN. Default PIN is ${LockerSecurityManager.DEFAULT_PIN}"
            _pinInput.value = ""
        }
    }

    fun onToggleAppLock(enabled: Boolean) {
        securityManager.setLockEnabled(enabled)
        _isLockActiveState.value = enabled
        _userNotification.value = if (enabled) "App lock enabled (PIN: ${securityManager.getSavedPin()})" else "App lock disabled"
    }

    fun onChangePin(newPin: String) {
        if (newPin.length == 4) {
            securityManager.setPin(newPin)
            _userNotification.value = "PIN updated to $newPin"
        }
    }

    fun skipLockForTesting() {
        securityManager.unlockAppDirectly()
        _pinInput.value = ""
        _pinErrorMessage.value = null
    }

    fun lockNow() {
        securityManager.lockApp()
        _pinInput.value = ""
        _pinErrorMessage.value = null
    }
}
