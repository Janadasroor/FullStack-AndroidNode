
// API Service
package com.janad.termuxexp.data.api

import com.janad.termuxexp.data.BrowseResponse
import com.janad.termuxexp.data.FileContent
import com.janad.termuxexp.data.SearchResponse
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface FileApiService {
    @GET("browse")
    suspend fun browseDirectory(@Query("path") path: String = ""): Response<BrowseResponse>

    @GET("file")
    suspend fun getFileContent(@Query("path") path: String): Response<FileContent>

    @POST("file")
    suspend fun saveFile(@Body request: SaveFileRequest): Response<ApiResponse>

    @DELETE("file")
    suspend fun deleteFile(@Query("path") path: String): Response<ApiResponse>

    @POST("directory")
    suspend fun createDirectory(@Body request: CreateDirectoryRequest): Response<ApiResponse>

    @DELETE("directory")
    suspend fun deleteDirectory(
        @Query("path") path: String,
        @Query("recursive") recursive: Boolean = true
    ): Response<ApiResponse>

    @POST("rename")
    suspend fun renameItem(@Body request: RenameRequest): Response<ApiResponse>

    @POST("copy")
    suspend fun copyItem(@Body request: CopyRequest): Response<ApiResponse>

    @POST("move")
    suspend fun moveItem(@Body request: MoveRequest): Response<ApiResponse>

    @GET("search")
    suspend fun searchFiles(
        @Query("q") query: String,
        @Query("path") path: String = "",
        @Query("types") types: String? = null,
        @Query("case") caseSensitive: Boolean = false
    ): Response<SearchResponse>
}

@Serializable
data class SaveFileRequest(
    val path: String,
    val content: String,
    val createDirectories: Boolean = true
)

@Serializable
data class CreateDirectoryRequest(
    val path: String,
    val recursive: Boolean = true
)

@Serializable
data class RenameRequest(
    val oldPath: String,
    val newName: String
)

@Serializable
data class CopyRequest(
    val sourcePath: String,
    val destinationPath: String
)

@Serializable
data class MoveRequest(
    val sourcePath: String,
    val destinationPath: String
)

@Serializable
data class ApiResponse(
    val message: String,
    val error: String? = null
)