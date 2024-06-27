<#
Android Project Package Name Updater Script
Author: Jeff Picklyk (jpicklyk at gmail dot com)

## Purpose

This PowerShell script automates the process of updating the package name in an Android/Kotlin
project. It performs the following tasks:

1. Creates a backup of the project
2. Moves files to the new package structure
3. Updates package references in code files
4. Removes old, empty package directories

## Prerequisites

- PowerShell 5.1 or later
- Write access to the project directory

## Usage

1. Place the `UpdatePackageName.ps1` script in your project's root directory.

2. Open a PowerShell terminal in your project's root directory.

3. Run the script:

   ```powershell
   .\UpdatePackageName.ps1

The script will prompt you to enter the new package name.

4. To perform a dry run (see what changes would be made without actually making them):
    .\UpdatePackageName.ps1 -DryRun

5. To delete the default git tracking folder
    .\UpdatePackageName.ps1 -DeleteGitFolder

## What the Script Does

Creates a timestamped backup of your project
Prompts for the new package name
Identifies directories matching the old package structure
Creates new directories for the new package structure
Moves files from the old to the new package structure
Updates package references in code files
Removes empty directories from the old package structure
Provides a summary of actions taken

## Configuration
The script uses several variables that you may need to modify based on your project structure:

$currentPackageName: The current package name (default: "com.example.starterapplication")
$fileExtensions: File types to update (default: ".kt", ".kts", ".java", ".xml", "*.conf")
$skipFolders: Folders to ignore (default: ".gradle", ".idea", ".git", ".kotlin", "gradle", "build")

Modify these variables at the top of the script if needed.

## Logging
The script creates a log file named UpdatePackageName.log in the project root directory. This log
contains detailed information about the script's actions and any errors encountered.

## Important Notes

Always run this script on a backed-up or version-controlled project.
Review the changes after running the script to ensure everything is correct.
The script may require modifications for projects with non-standard directory structures.
If you encounter any issues, check the log file for detailed information.

## Troubleshooting
If you encounter any issues:

Check the UpdatePackageName.log file for error messages.
Ensure you have write permissions in the project directory.
Verify that the $currentPackageName in the script matches your project's current package name.
Try running the script with the -DryRun flag to see what changes would be made without actually
making them.

## Contributing
Feel free to fork this script and adapt it to your needs. If you make improvements that could
benefit others, consider submitting a pull request.

## License
This script is provided "as is", without warranty of any kind. Use at your own risk.
#>

# Script parameters
param (
    [switch]$DryRun,
    [switch]$DeleteGitFolder
)

# Global variables
$currentPackageName = "com.example.starterapplication"
$rootProjectFolder = Get-Location
$logFile = "$rootProjectFolder\UpdatePackageName.log"
$fileExtensions = @("*.kt", "*.kts", "*.java", "*.xml", "*.conf")
$skipFolders = @(".gradle", ".idea", ".git", ".kotlin", "gradle", "build")

# Statistics
$stats = @{
    DirectoriesProcessed = 0
    FilesMoved = 0
    FilesUpdated = 0
}

# Functions
function Backup-ProjectFolder {
    param ([string]$rootProjectFolder)

    $parentFolder = Split-Path -Parent $rootProjectFolder
    $projectFolderName = Split-Path -Leaf $rootProjectFolder
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupFolderName = "${projectFolderName}_backup_${timestamp}"
    $backupPath = Join-Path -Path $parentFolder -ChildPath $backupFolderName

    try {
        "Creating backup of project folder..." | Out-File -FilePath $logFile -Append
        if (-not $DryRun) {
            Copy-Item -Path $rootProjectFolder -Destination $backupPath -Recurse -ErrorAction Stop
        }
        "Backup created successfully at: $backupPath" | Out-File -FilePath $logFile -Append
        return $true
    }
    catch {
        "Failed to create backup: $_" | Out-File -FilePath $logFile -Append
        return $false
    }
}

function Update-PackageNames {
    param (
        [string]$filePath,
        [string]$currentPackageName,
        [string]$newPackageName
    )
    try {
        if (Test-Path $filePath) {
            $content = Get-Content $filePath -Raw -ErrorAction Stop
            if ($content -match [regex]::Escape($currentPackageName)) {
                $newContent = $content -replace [regex]::Escape($currentPackageName), $newPackageName
                if (-not $DryRun) {
                    Set-Content -Path $filePath -Value $newContent -ErrorAction Stop
                }
                "Updated package names in $filePath" | Out-File -FilePath $logFile -Append
                $stats.FilesUpdated++
            }
        }
        else {
            "File not found: $filePath" | Out-File -FilePath $logFile -Append
        }
    } catch {
        "Failed to update package names in ${filePath}: $_" | Out-File -FilePath $logFile -Append
    }
}

function Get-BasePackagePath {
    param (
        [string]$fullPath,
        [string]$packagePath
    )
    $index = $fullPath.IndexOf($packagePath)
    if ($index -ge 0) {
        return $fullPath.Substring(0, $index)
    }
    return $null
}

function Process-Directory {
    param ([string]$dir)

    try {
        if (Test-Path $dir) {
            $items = Get-ChildItem -Path $dir -ErrorAction Stop
            foreach ($item in $items) {
                if ($item.PSIsContainer) {
                    if ($skipFolders -notcontains $item.Name) {
                        Process-Directory -dir $item.FullName
                    }
                } else {
                    if ($fileExtensions -contains ('*' + $item.Extension)) {
                        Update-PackageNames -filePath $item.FullName -currentPackageName $currentPackageName -newPackageName $newPackageName
                    }
                }
            }
        }
        else {
            "Directory not found, skipping: $dir" | Out-File -FilePath $logFile -Append
        }
    } catch {
        "Error processing directory $dir : $_" | Out-File -FilePath $logFile -Append
    }
}

# Main script execution
"Script started at $(Get-Date)" | Out-File -FilePath $logFile

# Prompt for new package name
do {
    $newPackageName = Read-Host "Enter the new package name (e.g., com.example.newapp)"
} while ([string]::IsNullOrEmpty($newPackageName))

# Create a backup of the project folder
$backupCreated = Backup-ProjectFolder -rootProjectFolder $rootProjectFolder
if (-not $backupCreated) {
    Write-Host "Failed to create backup. Aborting script."
    exit
}

# Convert package names to directory paths
$currentPackagePath = $currentPackageName -replace '\.', '\'
$newPackagePath = $newPackageName -replace '\.', '\'

# Find all directories that match the current package path, excluding skip folders
$oldPackageDirectories = Get-ChildItem -Path $rootProjectFolder -Recurse -Directory |
    Where-Object {
        $dir = $_
        $matchesPackagePath = $dir.FullName -match [regex]::Escape($currentPackagePath)
        $shouldSkip = $skipFolders | Where-Object { $dir.FullName -match "\\$_($|\\)" }
        $matchesPackagePath -and -not $shouldSkip
    }

"Found the following old package directories:" | Out-File -FilePath $logFile -Append
$oldPackageDirectories | ForEach-Object { $_.FullName | Out-File -FilePath $logFile -Append }

foreach ($dir in $oldPackageDirectories) {
    $newDir = $dir.FullName -replace [regex]::Escape($currentPackagePath), $newPackagePath

    "Processing directory: $($dir.FullName)" | Out-File -FilePath $logFile -Append
    "New directory: $newDir" | Out-File -FilePath $logFile -Append

    try {
        if (Test-Path $dir.FullName) {
            if (-not $DryRun) {
                if (-not (Test-Path $newDir)) {
                    New-Item -Path $newDir -ItemType Directory -Force | Out-Null
                }
            }
            "Created new directory: $newDir" | Out-File -FilePath $logFile -Append

            Get-ChildItem -Path $dir.FullName -File -ErrorAction Stop | ForEach-Object {
                $newFilePath = Join-Path $newDir $_.Name
                if (-not $DryRun) {
                    Move-Item -Path $_.FullName -Destination $newFilePath -Force
                }
                "Moved file: $($_.FullName) to $newFilePath" | Out-File -FilePath $logFile -Append
                $stats.FilesMoved++
            }

            $basePackagePath = Get-BasePackagePath -fullPath $dir.FullName -packagePath $currentPackagePath
            $currentDir = $dir.FullName
            while ($currentDir -ne $basePackagePath) {
                if (Test-Path $currentDir) {
                    if (-not (Get-ChildItem -Path $currentDir)) {
                        if (-not $DryRun) {
                            Remove-Item -Path $currentDir -Force
                        }
                        "Removed empty directory: $currentDir" | Out-File -FilePath $logFile -Append
                    } else {
                        "Directory not empty, stopping removal: $currentDir" | Out-File -FilePath $logFile -Append
                        break
                    }
                }
                $currentDir = Split-Path $currentDir -Parent
            }
            $stats.DirectoriesProcessed++
        }
        else {
            "Directory no longer exists, skipping: $($dir.FullName)" | Out-File -FilePath $logFile -Append
        }
    } catch {
        "Error processing directory $($dir.FullName): $_" | Out-File -FilePath $logFile -Append
    }
}

# Process all directories for file content updates
Process-Directory -dir $rootProjectFolder

# Delete .git folder if flag is set
if ($DeleteGitFolder) {
    $gitFolder = Join-Path -Path $rootProjectFolder -ChildPath ".git"
    if (Test-Path $gitFolder) {
        if (-not $DryRun) {
            Remove-Item -Path $gitFolder -Recurse -Force
            "Deleted .git folder" | Out-File -FilePath $logFile -Append
            Write-Host "Deleted .git folder"
        } else {
            "Would delete .git folder (dry run)" | Out-File -FilePath $logFile -Append
            Write-Host "Would delete .git folder (dry run)"
        }
    } else {
        "No .git folder found" | Out-File -FilePath $logFile -Append
        Write-Host "No .git folder found"
    }
} else {
    ".git folder was not deleted (use -DeleteGitFolder flag to delete)" | Out-File -FilePath $logFile -Append
    Write-Host ".git folder was not deleted (use -DeleteGitFolder flag to delete)"
}

# Print summary
$summary = @"
Script Execution Summary:
-------------------------
Directories Processed: $($stats.DirectoriesProcessed)
Files Moved: $($stats.FilesMoved)
Files Updated: $($stats.FilesUpdated)
Dry Run: $($DryRun)
.git Folder Deleted: $($DeleteGitFolder)
"@

$summary | Out-File -FilePath $logFile -Append
Write-Host $summary

"Script ended at $(Get-Date)" | Out-File -FilePath $logFile -Append
Write-Host "Package name update process completed. Check the log file at $logFile for details."