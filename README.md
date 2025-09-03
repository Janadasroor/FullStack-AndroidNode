# FullStack-AndroidNode

A full-stack file management system that enables remote file and folder operations through a REST API. Browse, create, and delete files and folders on your computer from any device over the network.

## Features

- **Remote File Management**: Browse, create, and delete files and folders remotely
- **Cross-Platform Access**: Access your files from mobile devices or any networked device
- **REST API**: Simple and intuitive API endpoints for file operations
- **Android Demo App**: Included Android application demonstrating API usage
- **Network Connectivity**:  access over local network or internet

## Prerequisites

- **Node.js**: Version 22.19.0 or higher
- **Git**: For cloning the repository
- **Visual Studio Code**: Recommended for development and port forwarding
- **Android Studio**: Required for building the Android demo app (optional)

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Janadasroor/FullStack-AndroidNode.git
   ```
     ```cmd
   git clone https://github.com/Janadasroor/FullStack-AndroidNode.git

2. **Navigate to Project Directory**
   ```bash
   cd FullStack-AndroidNode
   ```

3. **Install Dependencies**
   ```bash
   npm install
   ```

## Usage

### Starting the Server

1. **Launch the Server**
   ```bash & cmd
   node server.js
   ```
   The server will start on port 3000 by default.

2. **Handle Port Conflicts**
   If port 3000 is busy, modify the port in your configuration (e.g., use port 3031).

### Network Access Setup

1. **Configure Port Forwarding in VS Code**
   - Open the Ports panel in VS Code
   - Add port forwarding for your server port (default: 3000)
   - Set link visibility to **Public** to enable network access

2. **Obtain Your Access URL**
   - Copy the generated public URL (e.g., `https://example-3000.euw.devtunnels.ms/`)
   - This URL will be used to access your API over the network

### Android App Configuration

1. **Update Base URL**
   - Open the Android project
   - Locate the `BASE_URL` configuration
   - Replace the placeholder with your generated VS Code tunnel URL in this file:
   - FullStack-AndroidNode\android_project\TermuxEXP\app\src\main\java\com\janad\termuxexp\data\api\NetworkModule.kt
     ```Kotlin
     BASE_URL = "https://your-generated-url.devtunnels.ms/"
     ```

2. **Build and Run**
   - Refer to the Android project's README for detailed build instructions

## API Endpoints

The REST API provides the following functionality:
- Browse files and folders
- Create new files and folders
- Delete existing files and folders

*For detailed API documentation, please refer to the API documentation or explore the endpoints in the server code.*

## Project Structure

```
FullStack-AndroidNode/
├── server.js              # Main server file
├── package.json           # Node.js dependencies
├── android_project/           # Android demo application
└── README.md             # This file
```



## Security Note

This application opens file system access over the network. Ensure you:
- Use secure networks
- Implement proper authentication if deploying to production
- Be cautious about exposing sensitive files


