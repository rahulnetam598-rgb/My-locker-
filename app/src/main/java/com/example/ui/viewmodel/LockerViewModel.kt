package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.local.MyLockerDatabase
import com.example.data.preferences.LockerSecurityManager
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LockerUiState(
    val documents: List<DocumentEntity> = emptyList(),
    val filteredDocuments: List<DocumentEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null, // null means All
    val totalCount: Int = 0,
    val identityCount: Int = 0,
    val educationCount: Int = 0,
    val otherCount: Int = 0,
    val totalStorageBytes: Long = 0,
    val previewDocument: DocumentEntity? = null,
    val documentToEdit: DocumentEntity? = null,
    val documentToDelete: DocumentEntity? = null,
    val pendingImportUri: Uri? = null,
    val isAddDialogOpen: Boolean = false,
    val isSecurityDialogOpen: Boolean = false,
    val isDisclaimerDialogOpen: Boolean = false,
    val userNotification: String? = null,
    val isLockActive: Boolean = false,
    val isAppUnlocked: Boolean = true,
    val pinInput: String = "",
    val pinErrorMessage: String? = null
)

class LockerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MyLockerDatabase.getInstance(application)
    private val repository = DocumentRepository(application, database.documentDao())
    val securityManager = LockerSecurityManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _previewDocument = MutableStateFlow<DocumentEntity?>(null)
    val previewDocument = _previewDocument.asStateFlow()

    private val _documentToEdit = MutableStateFlow<DocumentEntity?>(null)
    val documentToEdit = _documentToEdit.asStateFlow()

    private val _documentToDelete = MutableStateFlow<DocumentEntity?>(null)
    val documentToDelete = _documentToDelete.asStateFlow()

    private val _pendingImportUri = MutableStateFlow<Uri?>(null)
    val pendingImportUri = _pendingImportUri.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen = _isAddDialogOpen.asStateFlow()

    private val _isSecurityDialogOpen = MutableStateFlow(false)
    val isSecurityDialogOpen = _isSecurityDialogOpen.asStateFlow()

    private val _isDisclaimerDialogOpen = MutableStateFlow(false)
    val isDisclaimerDialogOpen = _isDisclaimerDialogOpen.asStateFlow()

    private val _userNotification = MutableStateFlow<String?>(null)
    val userNotification = _userNotification.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput = _pinInput.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage = _pinErrorMessage.asStateFlow()

    private val _isLockActiveState = MutableStateFlow(securityManager.isLockEnabled())

    val uiState: StateFlow<LockerUiState> = combine(
        repository.allDocuments,
        _searchQuery,
        _selectedCategoryFilter,
        _previewDocument,
        _documentToEdit,
        _documentToDelete,
        _pendingImportUri,
        _isAddDialogOpen,
        _isSecurityDialogOpen,
        _isDisclaimerDialogOpen,
        _userNotification,
        _isLockActiveState,
        securityManager.isUnlocked,
        _pinInput,
        _pinErrorMessage
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val docs = args[0] as List<DocumentEntity>
        val query = args[1] as String
        val catFilter = args[2] as String?
        val preview = args[3] as DocumentEntity?
        val toEdit = args[4] as DocumentEntity?
        val toDelete = args[5] as DocumentEntity?
        val pendingUri = args[6] as Uri?
        val addOpen = args[7] as Boolean
        val secOpen = args[8] as Boolean
        val discOpen = args[9] as Boolean
        val notif = args[10] as String?
        val lockActive = args[11] as Boolean
        val unlocked = args[12] as Boolean
        val pin = args[13] as String
        val pinErr = args[14] as String?

        val filtered = docs.filter { doc ->
            val matchesCategory = catFilter == null || doc.category.equals(catFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                doc.title.contains(query, ignoreCase = true) ||
                doc.documentNumber.contains(query, ignoreCase = true) ||
                doc.notes.contains(query, ignoreCase = true) ||
                doc.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val idCount = docs.count { it.category.equals("IDENTITY", ignoreCase = true) }
        val eduCount = docs.count { it.category.equals("EDUCATION", ignoreCase = true) }
        val otherCount = docs.count { it.category.equals("OTHER", ignoreCase = true) }
        val totalBytes = docs.sumOf { it.fileSize }

        LockerUiState(
            documents = docs,
            filteredDocuments = filtered,
            searchQuery = query,
            selectedCategoryFilter = catFilter,
            totalCount = docs.size,
            identityCount = idCount,
            educationCount = eduCount,
            otherCount = otherCount,
            totalStorageBytes = totalBytes,
            previewDocument = preview,
            documentToEdit = toEdit,
            documentToDelete = toDelete,
            pendingImportUri = pendingUri,
            isAddDialogOpen = addOpen,
            isSecurityDialogOpen = secOpen,
            isDisclaimerDialogOpen = discOpen,
            userNotification = notif,
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

    fun onSelectDocumentForPreview(document: DocumentEntity?) {
        _previewDocument.value = document
    }

    fun onInitiateAddDocument(uri: Uri) {
        _pendingImportUri.value = uri
        _isAddDialogOpen.value = true
    }

    fun dismissAddDialog() {
        _pendingImportUri.value = null
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
                _userNotification.value = "Failed to add document: ${err.message}"
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
                _userNotification.value = "Failed to update: ${err.message}"
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
                _userNotification.value = "Failed to delete: ${err.message}"
            }
        }
    }

    fun restoreSampleDocuments() {
        viewModelScope.launch {
            repository.restoreSampleDocuments()
            _userNotification.value = "Sample documents restored for testing."
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
