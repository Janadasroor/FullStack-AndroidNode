
// Data Models
package com.janad.termuxexp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FileItem(
    val name: String,
    val type: String, // "file" or "directory"
    val path: String,
    val language: String? = null,
    val size: Long = 0L,
    val modified: String? = null,
    val isHidden: Boolean = false
)

@Serializable
data class BrowseResponse(
    val currentPath: String,
    val parentPath: String? = null,
    val items: List<FileItem>
)

@Serializable
data class FileContent(
    val path: String,
    val name: String,
    val language: String,
    val size: Long,
    val modified: String,
    val isBinary: Boolean,
    val content: String?
)

@Serializable
data class SearchMatch(
    val lineNumber: Int,
    val line: String,
    val match: String
)

@Serializable
data class SearchResult(
    val type: String,
    val name: String,
    val path: String,
    val language: String? = null,
    val matches: Map<String, JsonElement>? = null
)

@Serializable
data class SearchResponse(
    val query: String,
    val searchPath: String,
    val totalResults: Int,
    val results: List<SearchResult>
)
