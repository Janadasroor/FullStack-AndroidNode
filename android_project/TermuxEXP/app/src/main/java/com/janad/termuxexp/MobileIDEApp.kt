// MobileIDEApp.kt
package com.janad.termuxexp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.janad.termuxexp.presentation.screens.FileBrowserScreen
import com.janad.termuxexp.presentation.screens.FileEditorScreen
import com.janad.termuxexp.presentation.screens.SearchScreen
import com.janad.termuxexp.presentation.viewmodels.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileIDEApp() {
    val navController = rememberNavController()
    val fileViewModel: FileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "file_browser"
    ) {
        composable("file_browser") {
            FileBrowserScreen(
                viewModel = fileViewModel,
                onNavigateToEditor = { filePath ->
                    navController.navigate("file_editor/$filePath")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
            )
        }

        composable("file_editor/{filePath}") { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            FileEditorScreen(
                viewModel = fileViewModel,
                filePath = filePath,
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                viewModel = fileViewModel,
                onBack = { navController.popBackStack() },
                onFileSelected = { filePath ->
                    navController.navigate("file_editor/$filePath")
                }
            )
        }
    }
}
