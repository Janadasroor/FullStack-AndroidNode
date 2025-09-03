package com.janad.termuxexp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.janad.termuxexp.data.FileContent
import com.janad.termuxexp.presentation.viewmodels.FileViewModel
import com.janad.termuxexp.data.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileEditorScreen(
    viewModel: FileViewModel,
    filePath: String,
    onBack: () -> Unit
) {
    val currentFileContent by viewModel.currentFileContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var editableContent by remember { mutableStateOf("") }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    LaunchedEffect(filePath) {
        viewModel.openFile(filePath)
    }

    LaunchedEffect(currentFileContent) {
        currentFileContent?.content?.let { content ->
            editableContent = content
            hasUnsavedChanges = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentFileContent?.name ?: "Loading...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (currentFileContent?.language != null) {
                            Text(
                                text = currentFileContent!!.language.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasUnsavedChanges) {
                        IconButton(
                            onClick = {
                                viewModel.saveFile(filePath, editableContent)
                                hasUnsavedChanges = false
                            }
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    ErrorMessage(
                        error = error!!,
                        onRetry = { viewModel.openFile(filePath) },
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                currentFileContent?.isBinary == true -> {
                    BinaryFileViewer(
                        fileContent = currentFileContent!!,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    CodeEditor(
                        content = editableContent,
                        onContentChange = { newContent ->
                            editableContent = newContent
                            hasUnsavedChanges = currentFileContent?.content != newContent
                        },
                        language = currentFileContent?.language ?: "text",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CodeEditor(
    content: String,
    onContentChange: (String) -> Unit,
    language: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = modifier.padding(16.dp),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (content.isEmpty()) {
                    Text(
                        text = "Start typing...",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun BinaryFileViewer(
    fileContent: FileContent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Binary File",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Size: ${formatFileSize(fileContent.size)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Cannot display binary content",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Search Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: FileViewModel,
    onBack: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFileTypes by remember { mutableStateOf(setOf<String>()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val fileTypeOptions = listOf(
        "javascript", "typescript", "python", "java", "kotlin",
        "cpp", "c", "html", "css", "json", "markdown", "text"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (selectedFileTypes.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search files and content") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Search Button
            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        val types = if (selectedFileTypes.isNotEmpty()) {
                            selectedFileTypes.joinToString(",")
                        } else null
                        viewModel.searchFiles(searchQuery, types = types)
                    }
                },
                enabled = searchQuery.isNotEmpty() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    error != null -> {
                        ErrorMessage(
                            error = error!!,
                            onRetry = {
                                val types = if (selectedFileTypes.isNotEmpty()) {
                                    selectedFileTypes.joinToString(",")
                                } else null
                                viewModel.searchFiles(searchQuery, types = types)
                            },
                            onDismiss = { viewModel.clearError() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    searchResults.isNotEmpty() -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { result ->
                                SearchResultCard(
                                    result = result,
                                    onClick = { onFileSelected(result.path) }
                                )
                            }
                        }
                    }
                    searchQuery.isNotEmpty() && !isLoading -> {
                        Text(
                            text = "No results found",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            fileTypes = fileTypeOptions,
            selectedTypes = selectedFileTypes,
            onSelectionChange = { selectedFileTypes = it },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.type == "directory") Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (result.type == "directory") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (result.language != null) {
                    Text(
                        text = result.language.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = result.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Dialog Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDirectoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var directoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Directory") },
        text = {
            OutlinedTextField(
                value = directoryName,
                onValueChange = { directoryName = it },
                label = { Text("Directory name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(directoryName) },
                enabled = directoryName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create File") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(fileName) },
                enabled = fileName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FilterDialog(
    fileTypes: List<String>,
    selectedTypes: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by File Type") },
        text = {
            LazyColumn {
                items(fileTypes) { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedTypes.contains(type),
                            onCheckedChange = { isChecked ->
                                val newSelection = if (isChecked) {
                                    selectedTypes + type
                                } else {
                                    selectedTypes - type
                                }
                                onSelectionChange(newSelection)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type.uppercase(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onSelectionChange(emptySet())
                    onDismiss()
                }
            ) {
                Text("Clear All")
            }
        }
    )
}

@Composable
fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}