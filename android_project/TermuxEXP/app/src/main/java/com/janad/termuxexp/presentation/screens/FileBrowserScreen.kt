

// File Browser Screen
package com.janad.termuxexp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.janad.termuxexp.data.FileItem
import com.janad.termuxexp.presentation.viewmodels.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val fileItems by viewModel.fileItems.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mobile IDE")
                        if (currentPath.isNotEmpty()) {
                            Text(
                                text = "/$currentPath",

                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Directory")
                    }
                    IconButton(onClick = { showCreateFileDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create File")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        onRetry = { viewModel.browseDirectory(currentPath) },
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fileItems) { fileItem ->
                            FileItemCard(
                                fileItem = fileItem,
                                onItemClick = {
                                    if (fileItem.type == "directory") {
                                        viewModel.browseDirectory(fileItem.path)
                                    } else {
                                        onNavigateToEditor(fileItem.path)
                                    }
                                },
                                onDeleteClick = { viewModel.deleteFile(fileItem.path) },
                                onRenameClick = { newName ->
                                    viewModel.renameItem(fileItem.path, newName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDirectoryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { dirName ->
                val newPath = if (currentPath.isEmpty()) dirName else "$currentPath/$dirName"
                viewModel.createDirectory(newPath)
                showCreateDialog = false
            }
        )
    }

    if (showCreateFileDialog) {
        CreateFileDialog(
            onDismiss = { showCreateFileDialog = false },
            onConfirm = { fileName ->
                val newPath = if (currentPath.isEmpty()) fileName else "$currentPath/$fileName"
                onNavigateToEditor(newPath)
                showCreateFileDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileItemCard(
    fileItem: FileItem,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileIcon(fileItem),
                contentDescription = null,
                tint = getFileIconColor(fileItem),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fileItem.language != null) {
                        Text(
                            text = fileItem.language.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (fileItem.size > 0) {
                        Text(
                            text = formatFileSize(fileItem.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showRenameDialog = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            currentName = fileItem.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onRenameClick(newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
private fun getFileIcon(fileItem: FileItem): ImageVector {
    return when {
        fileItem.type == "directory" -> Icons.Default.Folder
        fileItem.language == "javascript" || fileItem.language == "typescript" -> Icons.Default.Code
        fileItem.language == "python" -> Icons.Default.Code
        fileItem.language == "java" || fileItem.language == "kotlin" -> Icons.Default.Code
        fileItem.language == "html" || fileItem.language == "css" -> Icons.Default.Web
        fileItem.language == "json" -> Icons.Default.DataObject
        fileItem.language == "markdown" -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

@Composable
private fun getFileIconColor(fileItem: FileItem) = when {
    fileItem.type == "directory" -> MaterialTheme.colorScheme.primary
    fileItem.language != null -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

fun formatFileSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        bytes >= gb -> String.format("%.1f GB", bytes.toFloat() / gb)
        bytes >= mb -> String.format("%.1f MB", bytes.toFloat() / mb)
        bytes >= kb -> String.format("%.1f KB", bytes.toFloat() / kb)
        else -> "$bytes B"
    }
}
