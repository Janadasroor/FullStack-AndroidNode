// MainActivity.kt
package com.janad.termuxexp

import RequestStoragePermission
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.janad.termuxexp.ui.theme.MobileIDEBrowserTheme
import dagger.hilt.android.AndroidEntryPoint
import hasStoragePermission

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Option 1: Request permission in onCreate


            MobileIDEBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermission by remember { mutableStateOf(hasStoragePermission()) }

                    if (hasPermission) {
                        MobileIDEApp()
                    } else {
                        RequestStoragePermission { granted ->
                            hasPermission = granted
                        }

                    }
                }
            }
        }
    }
}



