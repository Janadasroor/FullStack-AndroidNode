# Android File Manager App

An Android application that demonstrates remote file management capabilities by connecting to the FullStack-AndroidNode backend server. Built with modern Android technologies including Jetpack Compose, Retrofit, and Hilt for dependency injection.

## Features

- **Remote File Browsing**: Navigate through files and folders on your computer remotely
- **Modern UI**: Built with Jetpack Compose for a responsive, native Android experience
- **Network Operations**: Create, delete, and manage files over network connection
- **Real-time Updates**: Live synchronization with the backend server
- **Material Design**: Clean, intuitive interface following Material Design 3 guidelines

## Technology Stack

### Core Framework
- **Android SDK**: Target API 36 (Android 14+), Minimum API 24 (Android 7.0+)
- **Kotlin**: Primary development language with coroutines support
- **Jetpack Compose**: Modern UI toolkit for native Android development

### Architecture & Dependencies
- **Hilt**: Dependency injection framework
- **MVVM Pattern**: Clean architecture implementation
- **Navigation Component**: Type-safe navigation with Compose

### Networking
- **Retrofit 2.11.0**: Type-safe HTTP client for REST API communication
- **OkHttp 4.12.0**: HTTP client with logging interceptor for debugging
- **Kotlinx Serialization**: JSON serialization/deserialization

### UI Components
- **Material Design 3**: Latest Material Design components
- **Material Icons Extended**: Comprehensive icon library
- **Compose Foundation**: Core Compose building blocks

## Prerequisites

- **Android Studio**: Latest stable version (recommended: Hedgehog or newer)
- **Kotlin**: Version compatible with Kotlin Compose compiler
- **Java**: JDK 11 or higher
- **Android Device/Emulator**: Running Android 7.0 (API 24) or higher
- **FullStack-AndroidNode Backend**: Server must be running and accessible

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Janadasroor/FullStack-AndroidNode.git
   cd FullStack-AndroidNode/android-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open"
   - Navigate to the android_project/TermuxEXP directory

3. **Sync Dependencies**
   - Android Studio will automatically detect the `build.gradle.kts` file
   - Click "Sync Now" when prompted to download all dependencies

## Configuration

### Backend Connection Setup

1. **Obtain Server URL**
   - Start your Node.js backend server
   - Configure port forwarding in VS Code
   - Copy the generated public URL (e.g., `https://example-3000.euw.devtunnels.ms/`)

2. **Update Base URL**
   - Locate the API configuration file in your Android project
   - You can find it in FullStack-AndroidNode\android_project\TermuxEXP\app\src\main\java\com\janad\termuxexp\data\api\NetworkModule.kt
   - Update the `BASE_URL` constant:
   ```kotlin
   const val BASE_URL = "https://your-generated-url.devtunnels.ms/"
   ```

3. **Network Security Configuration**
   - Ensure your `network_security_config.xml` allows HTTPS connections
   - For development, you may need to allow HTTP traffic (not recommended for production)

## Building the Project

### Development Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Key Dependencies Explained

| Dependency | Purpose | Version |
|------------|---------|---------|
| Retrofit | REST API client | 2.11.0 |
| OkHttp | HTTP client with interceptors | 4.12.0 |
| Hilt | Dependency injection | 2.52 |
| Kotlinx Serialization | JSON parsing | 1.6.3 |
| Jetpack Compose BOM | UI framework version management | Latest |
| Material Design 3 | UI components | Latest |

## Usage

1. **Launch the App**
   - Install and open the app on your Android device
   - Ensure your device is connected to the same network as your backend server

2. **Connect to Server**
   - The app will automatically attempt to connect using the configured BASE_URL
   - You should see your computer's file system displayed

3. **File Operations**
   - **Browse**: Tap on folders to navigate
   - **Create**: Use the add button to create new files/folders
   - **Delete**: Long press or use context menu to delete items

## Troubleshooting

### Common Issues

1. **Connection Failed**
   - Verify the backend server is running
   - Check the BASE_URL configuration
   - Ensure network connectivity between devices

2. **Build Errors**
   - Clean and rebuild the project: `Build > Clean Project`
   - Invalidate caches: `File > Invalidate Caches and Restart`
   - Check Gradle sync status

3. **Network Security**
   - For HTTPS issues, verify SSL certificates
   - For local development, ensure network security config allows your domain


## Contributing

1. Fork the repository
2. Create a feature branch from `main`
3. Follow Android development best practices
4. Write unit tests for new features
5. Submit a pull request with detailed description

## Security Considerations

- **Network Traffic**: Use HTTPS in production environments
- **Permissions**: App requests minimal required permissions
- **Data Validation**: All server responses are validated before processing
- **Error Handling**: Graceful error handling prevents app crashes


## Support

For Android-specific issues:
- Check Android Studio's build output for compilation errors
- Review Logcat for runtime issues
- Ensure your development environment meets the prerequisites
- For backend connectivity issues, refer to the main project README
