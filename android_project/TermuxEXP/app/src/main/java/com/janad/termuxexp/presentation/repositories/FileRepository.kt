package com.janad.termuxexp.presentation.repositories

import android.util.Log
import com.janad.termuxexp.data.BrowseResponse
import com.janad.termuxexp.data.FileContent
import com.janad.termuxexp.data.SearchResponse
import com.janad.termuxexp.data.api.CreateDirectoryRequest
import com.janad.termuxexp.data.api.FileApiService
import com.janad.termuxexp.data.api.RenameRequest
import com.janad.termuxexp.data.api.SaveFileRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    private val apiService: FileApiService
) {
    suspend fun browseDirectory(path: String): Result<BrowseResponse> {
        return try {
            val response = apiService.browseDirectory(path)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to browse directory"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error browsing directory: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFileContent(path: String): Result<FileContent> {
        return try {
            val response = apiService.getFileContent(path)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get file content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveFile(path: String, content: String): Result<String> {
        return try {
            val response = apiService.saveFile(SaveFileRequest(path, content))
            if (response.isSuccessful) {
                Result.success("File saved successfully")
            } else {
                Result.failure(Exception("Failed to save file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(path: String): Result<String> {
        return try {
            val response = apiService.deleteFile(path)
            if (response.isSuccessful) {
                Result.success("File deleted successfully")
            } else {
                Result.failure(Exception("Failed to delete file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDirectory(path: String): Result<String> {
        return try {
            val response = apiService.createDirectory(CreateDirectoryRequest(path))
            if (response.isSuccessful) {
                Result.success("Directory created successfully")
            } else {
                Result.failure(Exception("Failed to create directory"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameItem(oldPath: String, newName: String): Result<String> {
        return try {
            val response = apiService.renameItem(RenameRequest(oldPath, newName))
            if (response.isSuccessful) {
                Result.success("Item renamed successfully")
            } else {
                Result.failure(Exception("Failed to rename item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFiles(query: String, path: String, types: String? = null): Result<SearchResponse> {
        return try {
            val response = apiService.searchFiles(query, path, types)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}