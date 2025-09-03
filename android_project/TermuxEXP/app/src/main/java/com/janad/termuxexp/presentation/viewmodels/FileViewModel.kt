package com.janad.termuxexp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janad.termuxexp.data.FileContent
import com.janad.termuxexp.data.FileItem
import com.janad.termuxexp.presentation.repositories.FileRepository
import com.janad.termuxexp.data.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: FileRepository
) : ViewModel() {

    private val _currentPath = MutableStateFlow("")
    val currentPath = _currentPath.asStateFlow()

    private val _fileItems = MutableStateFlow<List<FileItem>>(emptyList())
    val fileItems = _fileItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentFileContent = MutableStateFlow<FileContent?>(null)
    val currentFileContent = _currentFileContent.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _navigationStack = MutableStateFlow<List<String>>(listOf(""))
    val navigationStack = _navigationStack.asStateFlow()

    init {
        //This is current directory on your server machine
        //You can set it ".." to go back to the previous directory on your server machine
        browseDirectory(".")
    }

    fun browseDirectory(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.browseDirectory(path)
                .onSuccess { response ->
                    _currentPath.value = response.currentPath
                    _fileItems.value = response.items
                    updateNavigationStack(path)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error"
                }

            _isLoading.value = false
        }
    }

    fun openFile(filePath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getFileContent(filePath)
                .onSuccess { fileContent ->
                    _currentFileContent.value = fileContent
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to open file"
                }

            _isLoading.value = false
        }
    }

    fun saveFile(path: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.saveFile(path, content)
                .onSuccess { message ->
                    // Update current file content if it's the same file
                    _currentFileContent.value?.let { current ->
                        if (current.path == path) {
                            _currentFileContent.value = current.copy(content = content)
                        }
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to save file"
                }

            _isLoading.value = false
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.deleteFile(path)
                .onSuccess {
                    // Refresh current directory
                    browseDirectory(_currentPath.value)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to delete file"
                }

            _isLoading.value = false
        }
    }

    fun createDirectory(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.createDirectory(path)
                .onSuccess {
                    // Refresh current directory
                    browseDirectory(_currentPath.value)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to create directory"
                }

            _isLoading.value = false
        }
    }

    fun renameItem(oldPath: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.renameItem(oldPath, newName)
                .onSuccess {
                    // Refresh current directory
                    browseDirectory(_currentPath.value)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to rename item"
                }

            _isLoading.value = false
        }
    }

    fun searchFiles(query: String, path: String = "", types: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.searchFiles(query, path, types)
                .onSuccess { response ->
                    _searchResults.value = response.results
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Search failed"
                }

            _isLoading.value = false
        }
    }

    fun navigateBack(): Boolean {
        val stack = _navigationStack.value
        return if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _navigationStack.value = newStack
            browseDirectory(newStack.last())
            true
        } else {
            false
        }
    }

    private fun updateNavigationStack(path: String) {
        val currentStack = _navigationStack.value
        if (currentStack.isEmpty() || currentStack.last() != path) {
            _navigationStack.value = currentStack + path
        }
    }

    fun clearError() {
        _error.value = null
    }
}