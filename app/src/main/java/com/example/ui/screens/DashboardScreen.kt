package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DocumentEntity
import com.example.model.DocumentCategory
import com.example.ui.components.DashboardStatsCard
import com.example.ui.components.DocumentCard
import com.example.ui.components.LockerTopBar
import com.example.ui.theme.LockerBlueLight
import com.example.ui.theme.LockerBluePrimary
import com.example.ui.viewmodel.LockerUiState
import com.example.ui.viewmodel.LockerViewModel
import com.example.util.PdfUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LockerViewModel,
    uiState: LockerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDisclaimerBanner by remember { mutableStateOf(true) }

    // System File Picker for PDF and Images (Requirement 3)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onInitiateAddDocument(uri)
        }
    }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LockerTopBar(
                isLockActive = uiState.isLockActive,
                onSecurityClicked = { viewModel.openSecurityDialog() },
                onDisclaimerClicked = { viewModel.openDisclaimerDialog() },
                onRestoreSamplesClicked = { viewModel.restoreSampleDocuments() },
                onLockNowClicked = { viewModel.lockNow() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                },
                containerColor = LockerBluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
                    .testTag("add_document_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Document"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add File",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFD)),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Stats Card (Dashboard count, category pills)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DashboardStatsCard(
                        totalCount = uiState.totalCount,
                        identityCount = uiState.identityCount,
                        educationCount = uiState.educationCount,
                        otherCount = uiState.otherCount,
                        totalStorageBytes = uiState.totalStorageBytes,
                        selectedCategory = uiState.selectedCategoryFilter,
                        onCategorySelected = { cat -> viewModel.onCategoryFilterSelected(cat) }
                    )
                }
            }

            // Disclaimer Banner (Requirement 11)
            item {
                AnimatedVisibility(visible = showDisclaimerBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F0FE))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = LockerBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personal wallet • Local storage only • Not connected to government databases",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF042A68)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showDisclaimerBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    tint = Color(0xFF042A68),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar (Requirement 5)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search by name, ID number, or category...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = LockerBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("document_search_bar")
                    )
                }
            }

            // Category Filter Chips (Requirement 4)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { viewModel.onCategoryFilterSelected(null) },
                            label = { Text("All (${uiState.totalCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LockerBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_all")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == "IDENTITY",
                            onClick = {
                                if (uiState.selectedCategoryFilter == "IDENTITY") viewModel.onCategoryFilterSelected(null)
                                else viewModel.onCategoryFilterSelected("IDENTITY")
                            },
                            label = { Text("Identity (${uiState.identityCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LockerBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_identity")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == "EDUCATION",
                            onClick = {
                                if (uiState.selectedCategoryFilter == "EDUCATION") viewModel.onCategoryFilterSelected(null)
                                else viewModel.onCategoryFilterSelected("EDUCATION")
                            },
                            label = { Text("Education (${uiState.educationCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LockerBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_education")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == "OTHER",
                            onClick = {
                                if (uiState.selectedCategoryFilter == "OTHER") viewModel.onCategoryFilterSelected(null)
                                else viewModel.onCategoryFilterSelected("OTHER")
                            },
                            label = { Text("Other (${uiState.otherCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LockerBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_other")
                        )
                    }
                }
            }

            // Document Count header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedCategoryFilter != null) {
                            "${DocumentCategory.fromId(uiState.selectedCategoryFilter).title} Documents (${uiState.filteredDocuments.size})"
                        } else {
                            "All Documents (${uiState.filteredDocuments.size})"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )

                    if (uiState.searchQuery.isNotBlank()) {
                        Text(
                            text = "Matching '${uiState.searchQuery}'",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Documents List
            if (uiState.filteredDocuments.isEmpty()) {
                item {
                    EmptyDocumentsState(
                        isFiltered = uiState.searchQuery.isNotEmpty() || uiState.selectedCategoryFilter != null,
                        onAddClicked = {
                            filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                        },
                        onRestoreSamples = {
                            viewModel.restoreSampleDocuments()
                        }
                    )
                }
            } else {
                items(
                    items = uiState.filteredDocuments,
                    key = { it.id }
                ) { document ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        DocumentCard(
                            document = document,
                            onClick = { viewModel.onSelectDocumentForPreview(document) },
                            onEdit = { viewModel.onInitiateEdit(document) },
                            onDelete = { viewModel.onInitiateDelete(document) },
                            onShare = {
                                PdfUtils.shareDocument(
                                    context = context,
                                    file = File(document.filePath),
                                    mimeType = document.mimeType,
                                    title = document.title
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDocumentsState(
    isFiltered: Boolean,
    onAddClicked: () -> Unit,
    onRestoreSamples: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LockerBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = LockerBluePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isFiltered) "No Matching Documents" else "Your Locker is Empty",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFiltered) {
                    "No files match your current search query or category filter."
                } else {
                    "Securely add PDFs or photos of your identity cards, certificates, and personal files."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onAddClicked,
                    modifier = Modifier.testTag("empty_state_add_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select File")
                }

                if (!isFiltered) {
                    OutlinedButton(
                        onClick = onRestoreSamples,
                        modifier = Modifier.testTag("empty_state_samples_button")
                    ) {
                        Text("Load Samples")
                    }
                }
            }
        }
    }
}
