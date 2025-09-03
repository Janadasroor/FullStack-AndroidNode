import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Simple function to check permission
fun ComponentActivity.hasStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }
}

// Composable for handling storage permissions
@Composable
fun RequestStoragePermission(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    // Launcher for runtime permissions (Android 10 and below)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        onPermissionResult(allGranted)
    }

    // Launcher for settings (Android 11+)
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val hasPermission = activity.hasStoragePermission()
        onPermissionResult(hasPermission)
    }

    // Function to request permission
    fun requestPermission() {
        if (activity.hasStoragePermission()) {
            onPermissionResult(true)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Open settings
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${activity.packageName}")
            settingsLauncher.launch(intent)
        } else {
            // Android 10 and below - Request permissions
            permissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    // Auto-request on first composition
    LaunchedEffect(Unit) {
        requestPermission()
    }
}

/*
USAGE IN YOUR MAIN ACTIVITY:

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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

OR MANUAL REQUEST IN COMPOSE:

@Composable
fun MobileIDEApp() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    var hasPermission by remember { mutableStateOf(activity.hasStoragePermission()) }
    var showPermissionRequest by remember { mutableStateOf(false) }

    if (hasPermission) {
        // Your main content
        Text("App content here")
    } else {
        if (showPermissionRequest) {
            RequestStoragePermission { granted ->
                hasPermission = granted
                showPermissionRequest = false
            }
        } else {
            Button(onClick = { showPermissionRequest = true }) {
                Text("Grant Storage Permission")
            }
        }
    }
}

ADD TO AndroidManifest.xml:

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
*/