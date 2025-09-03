const express = require("express");
const fs = require("fs").promises;
const fsSync = require("fs");
const path = require("path");
const cors = require("cors");
const multer = require("multer");

const app = express();
const PORT = process.env.PORT || 3000;

// Set the root directory you want to browse
//Project directory
const ROOT_DIR = path.resolve(".");

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadPath = req.body.path ? path.join(ROOT_DIR, req.body.path) : ROOT_DIR;
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  }
});
const upload = multer({ storage });

// Helper function to check if path is within root
const isWithinRoot = (filePath) => {
  const resolved = path.resolve(filePath);
  return resolved.startsWith(ROOT_DIR);
};

// Helper function to get file stats
const getFileStats = async (filePath) => {
  try {
    const stats = await fs.stat(filePath);
    return {
      size: stats.size,
      modified: stats.mtime,
      created: stats.birthtime,
      isDirectory: stats.isDirectory(),
      permissions: stats.mode
    };
  } catch (error) {
    return null;
  }
};

// Helper function to detect file type/language
const detectFileType = (fileName) => {
  const ext = path.extname(fileName).toLowerCase();
  const typeMap = {
    '.js': 'javascript',
    '.jsx': 'javascript',
    '.ts': 'typescript',
    '.tsx': 'typescript',
    '.py': 'python',
    '.java': 'java',
    '.cpp': 'cpp',
    '.c': 'c',
    '.h': 'c',
    '.css': 'css',
    '.scss': 'scss',
    '.sass': 'sass',
    '.html': 'html',
    '.xml': 'xml',
    '.json': 'json',
    '.md': 'markdown',
    '.sql': 'sql',
    '.sh': 'shell',
    '.php': 'php',
    '.rb': 'ruby',
    '.go': 'go',
    '.rs': 'rust',
    '.kt': 'kotlin',
    '.swift': 'swift',
    '.yaml': 'yaml',
    '.yml': 'yaml',
    '.dockerfile': 'dockerfile',
    '.gitignore': 'text',
    '.env': 'text',
    '.txt': 'text'
  };
  
  return typeMap[ext] || 'text';
};

// Root endpoint
app.get("/", (req, res) => {
  res.json({
    message: "Enhanced File Browser API for Mobile IDE",
    endpoints: [
      "GET /browse?path= - Browse directories",
      "GET /file?path= - Read file content",
      "POST /file - Create or update file",
      "DELETE /file?path= - Delete file",
      "POST /directory - Create directory",
      "DELETE /directory?path= - Delete directory",
      "POST /rename - Rename file or directory",
      "POST /copy - Copy file or directory",
      "POST /move - Move file or directory",
      "POST /upload - Upload files",
      "GET /search?q=&path= - Search files",
      "GET /tree?path= - Get directory tree"
    ]
  });
});

// Browse endpoint - enhanced with more file info
app.get("/browse", async (req, res) => {
  try {
    let dirPath = req.query.path ? path.join(ROOT_DIR, req.query.path) : ROOT_DIR;
    
    //You can backtrack by using '..' in the path on android app
    //But you need to comment the following if statement bellow to allow it
    if (!isWithinRoot(dirPath)) {
      return res.status(403).json({ error: "Access denied" });
    }
 
    const files = await fs.readdir(dirPath, { withFileTypes: true });
    const items = await Promise.all(
      files.map(async (file) => {
        const fullPath = path.join(dirPath, file.name);
        const relativePath = path.relative(ROOT_DIR, fullPath);
        const stats = await getFileStats(fullPath);
        
        return {
          name: file.name,
          type: file.isDirectory() ? "directory" : "file",
          path: relativePath,
          language: file.isDirectory() ? null : detectFileType(file.name),
          size: stats?.size || 0,
          modified: stats?.modified,
          isHidden: file.name.startsWith('.')
        };
      })
    );

    // Sort: directories first, then files, both alphabetically
    items.sort((a, b) => {
      if (a.type !== b.type) {
        return a.type === 'directory' ? -1 : 1;
      }
      return a.name.localeCompare(b.name);
    });

    res.json({
      currentPath: path.relative(ROOT_DIR, dirPath),
      parentPath: dirPath === ROOT_DIR ? null : path.relative(ROOT_DIR, path.dirname(dirPath)),
      items
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to read directory", details: error.message });
  }
});

// Read file content
app.get("/file", async (req, res) => {
  try {
    const filePath = req.query.path ? path.join(ROOT_DIR, req.query.path) : null;
    
    if (!filePath || !isWithinRoot(filePath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    const stats = await getFileStats(filePath);
    if (!stats || stats.isDirectory) {
      return res.status(400).json({ error: "Not a file" });
    }

    // Check if file is binary
    const isBinary = await new Promise((resolve) => {
      const stream = fsSync.createReadStream(filePath);
      let chunk;
      stream.on('data', (data) => {
        chunk = data;
        stream.destroy();
      });
      stream.on('close', () => {
        if (chunk) {
          // Simple binary detection - look for null bytes
          resolve(chunk.includes(0));
        } else {
          resolve(false);
        }
      });
      stream.on('error', () => resolve(true));
    });

    if (isBinary) {
      return res.json({
        path: req.query.path,
        name: path.basename(filePath),
        language: detectFileType(path.basename(filePath)),
        size: stats.size,
        modified: stats.modified,
        isBinary: true,
        content: null
      });
    }

    const content = await fs.readFile(filePath, 'utf8');
    
    res.json({
      path: req.query.path,
      name: path.basename(filePath),
      language: detectFileType(path.basename(filePath)),
      size: stats.size,
      modified: stats.modified,
      isBinary: false,
      content
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to read file", details: error.message });
  }
});

// Create or update file
app.post("/file", async (req, res) => {
  try {
    const { path: filePath, content, createDirectories = false } = req.body;
    
    if (!filePath) {
      return res.status(400).json({ error: "File path is required" });
    }

    const fullPath = path.join(ROOT_DIR, filePath);
    
    if (!isWithinRoot(fullPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    // Create directories if needed
    if (createDirectories) {
      await fs.mkdir(path.dirname(fullPath), { recursive: true });
    }

    await fs.writeFile(fullPath, content || '', 'utf8');
    const stats = await getFileStats(fullPath);
    
    res.json({
      message: "File saved successfully",
      path: filePath,
      size: stats?.size || 0,
      modified: stats?.modified
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to save file", details: error.message });
  }
});

// Delete file
app.delete("/file", async (req, res) => {
  try {
    const filePath = req.query.path ? path.join(ROOT_DIR, req.query.path) : null;
    
    if (!filePath || !isWithinRoot(filePath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await fs.unlink(filePath);
    res.json({ message: "File deleted successfully" });
  } catch (error) {
    res.status(500).json({ error: "Unable to delete file", details: error.message });
  }
});

// Create directory
app.post("/directory", async (req, res) => {
  try {
    const { path: dirPath, recursive = true } = req.body;
    
    if (!dirPath) {
      return res.status(400).json({ error: "Directory path is required" });
    }

    const fullPath = path.join(ROOT_DIR, dirPath);
    
    if (!isWithinRoot(fullPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await fs.mkdir(fullPath, { recursive });
    res.json({ message: "Directory created successfully", path: dirPath });
  } catch (error) {
    res.status(500).json({ error: "Unable to create directory", details: error.message });
  }
});

// Delete directory
app.delete("/directory", async (req, res) => {
  try {
    const dirPath = req.query.path ? path.join(ROOT_DIR, req.query.path) : null;
    const recursive = req.query.recursive === 'true';
    
    if (!dirPath || !isWithinRoot(dirPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await fs.rmdir(dirPath, { recursive });
    res.json({ message: "Directory deleted successfully" });
  } catch (error) {
    res.status(500).json({ error: "Unable to delete directory", details: error.message });
  }
});

// Rename file or directory
app.post("/rename", async (req, res) => {
  try {
    const { oldPath, newName } = req.body;
    
    if (!oldPath || !newName) {
      return res.status(400).json({ error: "Old path and new name are required" });
    }

    const fullOldPath = path.join(ROOT_DIR, oldPath);
    const fullNewPath = path.join(path.dirname(fullOldPath), newName);
    
    if (!isWithinRoot(fullOldPath) || !isWithinRoot(fullNewPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await fs.rename(fullOldPath, fullNewPath);
    const newRelativePath = path.relative(ROOT_DIR, fullNewPath);
    
    res.json({ 
      message: "Renamed successfully",
      oldPath,
      newPath: newRelativePath
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to rename", details: error.message });
  }
});

// Copy file or directory
app.post("/copy", async (req, res) => {
  try {
    const { sourcePath, destinationPath } = req.body;
    
    if (!sourcePath || !destinationPath) {
      return res.status(400).json({ error: "Source and destination paths are required" });
    }

    const fullSourcePath = path.join(ROOT_DIR, sourcePath);
    const fullDestPath = path.join(ROOT_DIR, destinationPath);
    
    if (!isWithinRoot(fullSourcePath) || !isWithinRoot(fullDestPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    // Recursive copy function
    const copyRecursive = async (src, dest) => {
      const stats = await fs.stat(src);
      if (stats.isDirectory()) {
        await fs.mkdir(dest, { recursive: true });
        const files = await fs.readdir(src);
        await Promise.all(
          files.map(file => 
            copyRecursive(path.join(src, file), path.join(dest, file))
          )
        );
      } else {
        await fs.copyFile(src, dest);
      }
    };

    await copyRecursive(fullSourcePath, fullDestPath);
    
    res.json({
      message: "Copied successfully",
      sourcePath,
      destinationPath
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to copy", details: error.message });
  }
});

// Move file or directory
app.post("/move", async (req, res) => {
  try {
    const { sourcePath, destinationPath } = req.body;
    
    if (!sourcePath || !destinationPath) {
      return res.status(400).json({ error: "Source and destination paths are required" });
    }

    const fullSourcePath = path.join(ROOT_DIR, sourcePath);
    const fullDestPath = path.join(ROOT_DIR, destinationPath);
    
    if (!isWithinRoot(fullSourcePath) || !isWithinRoot(fullDestPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await fs.rename(fullSourcePath, fullDestPath);
    
    res.json({
      message: "Moved successfully",
      sourcePath,
      destinationPath
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to move", details: error.message });
  }
});

// Upload files
app.post("/upload", upload.array('files'), (req, res) => {
  try {
    const uploadedFiles = req.files.map(file => ({
      originalName: file.originalname,
      filename: file.filename,
      size: file.size,
      path: path.relative(ROOT_DIR, file.path)
    }));
    
    res.json({
      message: "Files uploaded successfully",
      files: uploadedFiles
    });
  } catch (error) {
    res.status(500).json({ error: "Upload failed", details: error.message });
  }
});

// Search files
app.get("/search", async (req, res) => {
  try {
    const query = req.query.q;
    const searchPath = req.query.path ? path.join(ROOT_DIR, req.query.path) : ROOT_DIR;
    const fileTypes = req.query.types ? req.query.types.split(',') : null;
    const caseSensitive = req.query.case === 'true';
    
    if (!query) {
      return res.status(400).json({ error: "Search query is required" });
    }

    if (!isWithinRoot(searchPath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    const results = [];
    const searchRegex = new RegExp(query, caseSensitive ? 'g' : 'gi');

    const searchRecursive = async (dirPath) => {
      const files = await fs.readdir(dirPath, { withFileTypes: true });
      
      await Promise.all(
        files.map(async (file) => {
          const fullPath = path.join(dirPath, file.name);
          const relativePath = path.relative(ROOT_DIR, fullPath);
          
          // Skip hidden files and directories
          if (file.name.startsWith('.')) return;
          
          if (file.isDirectory()) {
            // Search in directory name
            if (searchRegex.test(file.name)) {
              results.push({
                type: 'directory',
                name: file.name,
                path: relativePath,
                match: 'filename'
              });
            }
            await searchRecursive(fullPath);
          } else {
            const fileType = detectFileType(file.name);
            
            // Filter by file types if specified
            if (fileTypes && !fileTypes.includes(fileType)) return;
            
            // Search in filename
            const filenameMatch = searchRegex.test(file.name);
            
            // Search in file content for text files
            let contentMatches = [];
            try {
              if (fileType !== 'binary') {
                const content = await fs.readFile(fullPath, 'utf8');
                const lines = content.split('\n');
                lines.forEach((line, lineNumber) => {
                  if (searchRegex.test(line)) {
                    contentMatches.push({
                      lineNumber: lineNumber + 1,
                      line: line.trim(),
                      match: line.match(searchRegex)?.[0]
                    });
                  }
                });
              }
            } catch (error) {
              // Skip files that can't be read
            }
            
            if (filenameMatch || contentMatches.length > 0) {
              results.push({
                type: 'file',
                name: file.name,
                path: relativePath,
                language: fileType,
                matches: {
                  filename: filenameMatch,
                  content: contentMatches.slice(0, 10) // Limit to first 10 matches per file
                }
              });
            }
          }
        })
      );
    };

    await searchRecursive(searchPath);
    
    res.json({
      query,
      searchPath: path.relative(ROOT_DIR, searchPath),
      totalResults: results.length,
      results: results.slice(0, 100) // Limit total results
    });
  } catch (error) {
    res.status(500).json({ error: "Search failed", details: error.message });
  }
});

// Get directory tree
app.get("/tree", async (req, res) => {
  try {
    const treePath = req.query.path ? path.join(ROOT_DIR, req.query.path) : ROOT_DIR;
    const maxDepth = parseInt(req.query.depth) || 3;
    
    if (!isWithinRoot(treePath)) {
      return res.status(403).json({ error: "Access denied" });
    }

    const buildTree = async (dirPath, currentDepth = 0) => {
      if (currentDepth >= maxDepth) return null;
      
      const files = await fs.readdir(dirPath, { withFileTypes: true });
      const children = [];
      
      for (const file of files) {
        if (file.name.startsWith('.')) continue; // Skip hidden files
        
        const fullPath = path.join(dirPath, file.name);
        const relativePath = path.relative(ROOT_DIR, fullPath);
        
        const node = {
          name: file.name,
          path: relativePath,
          type: file.isDirectory() ? 'directory' : 'file'
        };
        
        if (file.isDirectory() && currentDepth < maxDepth - 1) {
          const subtree = await buildTree(fullPath, currentDepth + 1);
          if (subtree) {
            node.children = subtree;
          }
        } else if (!file.isDirectory()) {
          node.language = detectFileType(file.name);
        }
        
        children.push(node);
      }
      
      return children;
    };

    const tree = await buildTree(treePath);
    
    res.json({
      path: path.relative(ROOT_DIR, treePath),
      maxDepth,
      tree
    });
  } catch (error) {
    res.status(500).json({ error: "Unable to build tree", details: error.message });
  }
});

// Download file (keeping original functionality)
app.get("/download", (req, res) => {
  const filePath = req.query.path ? path.join(ROOT_DIR, req.query.path) : null;
  
  if (!filePath || !isWithinRoot(filePath)) {
    return res.status(403).json({ error: "Access denied" });
  }
  
  res.download(filePath);
});

// Error handling middleware
app.use((error, req, res, next) => {
  console.error('Server error:', error);
  res.status(500).json({ error: "Internal server error", details: error.message });
});

// Start the server on port 3000
app.listen(3000, "0.0.0.0", () => {
    console.log("Server running on port 3000");
});
//To access this server over network you need to use port forwarding in vs code and don't forget to change link visibility to public
