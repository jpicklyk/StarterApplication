#!/usr/bin/env bash
#
# Android Project Package Name Updater Script (Mac/Linux)
# Author: Jeff Picklyk (jpicklyk at gmail dot com)
#
# Purpose
# -------
# This script automates the process of renaming an Android/Kotlin project. It handles
# three distinct transformations:
#
# 1. Package name (dotted):   com.example.starterapplication -> com.mycompany.myapp
# 2. App display name:        Starter Application -> My App
# 3. PascalCase project name: StarterApplication -> MyApp (theme names, rootProject.name, etc.)
#
# It also:
# - Creates a backup of the project before making changes
# - Moves source files to the new package directory structure
# - Removes old, empty package directories
#
# Prerequisites
# -------------
# - Bash 3.2+ (macOS default) or any modern Bash
# - perl (pre-installed on macOS and virtually all Linux distros)
# - Write access to the project directory
#
# Usage
# -----
#   ./update_package_name.sh              # Interactive rename
#   ./update_package_name.sh --dry-run    # Preview changes without modifying files
#   ./update_package_name.sh --delete-git # Also delete the .git folder after renaming
#
# The script will prompt you to enter:
# - The new package name (e.g., com.mycompany.myapp)
# - The new app display name (e.g., My App)
# - Confirmation of the derived PascalCase project name (e.g., MyApp)
#
# Excluded Directories
# --------------------
# Knox library modules are excluded from all modifications since they have their own
# independent package identities:
# - knox-core/       (net.sfelabs.knox.core.*)
# - knox-enterprise/ (net.sfelabs.knox_enterprise)
# - knox-licensing/  (com.github.jpicklyk.knox.licensing)
# - knox-hilt/       (net.sfelabs.knox.hilt)
#
# Configuration
# -------------
# Modify the variables at the top of the script to match your project:
#   CURRENT_PACKAGE_NAME, CURRENT_APP_DISPLAY_NAME, CURRENT_PASCAL_CASE_NAME,
#   FILE_EXTENSIONS, SKIP_FOLDERS
#
# License
# -------
# This script is provided "as is", without warranty of any kind. Use at your own risk.

set -o pipefail

# ---- Configuration ----
CURRENT_PACKAGE_NAME="com.example.starterapplication"
CURRENT_APP_DISPLAY_NAME="Starter Application"
CURRENT_PASCAL_CASE_NAME="StarterApplication"

FILE_EXTENSIONS=("kt" "kts" "java" "xml" "conf" "toml" "properties" "pro")
SKIP_FOLDERS=(".gradle" ".idea" ".git" ".kotlin" "build" "knox-core" "knox-enterprise" "knox-licensing" "knox-hilt")

ROOT_DIR="$(pwd)"
LOG_FILE="$ROOT_DIR/UpdatePackageName.log"

# ---- Flags ----
DRY_RUN=false
DELETE_GIT_FOLDER=false

# ---- Statistics ----
DIRS_PROCESSED=0
FILES_MOVED=0
FILES_UPDATED=0

# ---- Parse arguments ----
while [[ $# -gt 0 ]]; do
    case "$1" in
        --dry-run)     DRY_RUN=true; shift ;;
        --delete-git)  DELETE_GIT_FOLDER=true; shift ;;
        -h|--help)
            echo "Usage: $0 [--dry-run] [--delete-git]"
            echo "  --dry-run     Show what would be changed without making modifications"
            echo "  --delete-git  Delete the .git folder after renaming"
            exit 0
            ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

# ---- Helper functions ----
log() {
    echo "$1" >> "$LOG_FILE"
}

should_skip() {
    local path="$1"
    for folder in "${SKIP_FOLDERS[@]}"; do
        if [[ "$path" == *"/$folder/"* ]] || [[ "$path" == *"/$folder" ]]; then
            return 0
        fi
    done
    return 1
}

# ---- Backup ----
backup_project() {
    local parent_dir
    parent_dir="$(dirname "$ROOT_DIR")"
    local project_name
    project_name="$(basename "$ROOT_DIR")"
    local timestamp
    timestamp="$(date +%Y%m%d_%H%M%S)"
    local backup_path="$parent_dir/${project_name}_backup_${timestamp}"

    log "Creating backup of project folder..."
    if [ "$DRY_RUN" = true ]; then
        log "Would create backup at: $backup_path (dry run)"
        return 0
    fi

    if cp -a "$ROOT_DIR" "$backup_path" 2>>"$LOG_FILE"; then
        log "Backup created successfully at: $backup_path"
        return 0
    else
        log "Failed to create backup"
        return 1
    fi
}

# ---- Update file contents ----
update_file_contents() {
    local file="$1"
    local needs_update=false

    if grep -Fqi "$CURRENT_PACKAGE_NAME" "$file" 2>/dev/null; then
        needs_update=true
    fi
    if grep -Fq "$CURRENT_PASCAL_CASE_NAME" "$file" 2>/dev/null; then
        needs_update=true
    fi
    if grep -Fqi "$CURRENT_APP_DISPLAY_NAME" "$file" 2>/dev/null; then
        needs_update=true
    fi

    if [ "$needs_update" = true ]; then
        if [ "$DRY_RUN" = false ]; then
            # Use perl for portable in-place replacement via environment variables.
            # Order: package name first (most specific), then PascalCase (case-sensitive),
            # then display name (case-insensitive).
            CURRENT_PKG="$CURRENT_PACKAGE_NAME" \
            NEW_PKG="$NEW_PACKAGE_NAME" \
            CURRENT_PASCAL="$CURRENT_PASCAL_CASE_NAME" \
            NEW_PASCAL="$NEW_PASCAL_CASE_NAME" \
            CURRENT_DISPLAY="$CURRENT_APP_DISPLAY_NAME" \
            NEW_DISPLAY="$NEW_APP_DISPLAY_NAME" \
            perl -i -pe '
                s/\Q$ENV{CURRENT_PKG}\E/$ENV{NEW_PKG}/gi;
                s/\Q$ENV{CURRENT_PASCAL}\E/$ENV{NEW_PASCAL}/g;
                s/\Q$ENV{CURRENT_DISPLAY}\E/$ENV{NEW_DISPLAY}/gi;
            ' "$file"
        fi
        log "Updated contents in $file"
        : $((FILES_UPDATED++))
    fi
}

# ==============================================================================
# Main
# ==============================================================================
echo "Script started at $(date)" > "$LOG_FILE"

# ---- Prompts ----
while true; do
    read -rp "Enter the new package name (e.g., com.mycompany.myapp): " NEW_PACKAGE_NAME
    [ -n "$NEW_PACKAGE_NAME" ] && break
done

while true; do
    read -rp "Enter the new app display name (e.g., My App): " NEW_APP_DISPLAY_NAME
    [ -n "$NEW_APP_DISPLAY_NAME" ] && break
done

# Derive PascalCase project name (remove spaces from display name)
NEW_PASCAL_CASE_NAME="${NEW_APP_DISPLAY_NAME// /}"
echo "Derived project name: $NEW_PASCAL_CASE_NAME"
read -rp "Press Enter to accept or type a different project name: " override
if [ -n "$override" ]; then
    NEW_PASCAL_CASE_NAME="$override"
fi

# ---- Show summary and confirm ----
echo ""
echo "Rename plan:"
echo "  Package:      $CURRENT_PACKAGE_NAME -> $NEW_PACKAGE_NAME"
echo "  Display name: $CURRENT_APP_DISPLAY_NAME -> $NEW_APP_DISPLAY_NAME"
echo "  Project name: $CURRENT_PASCAL_CASE_NAME -> $NEW_PASCAL_CASE_NAME"
echo ""
echo "Excluded directories: knox-core, knox-enterprise, knox-licensing, knox-hilt"
echo ""
if [ "$DRY_RUN" = false ]; then
    read -rp "Proceed? (Y/N): " confirm
    case "$confirm" in
        [Yy]|[Yy]es) ;;
        *) echo "Aborted."; exit 0 ;;
    esac
fi

# ---- Backup ----
if ! backup_project; then
    echo "Failed to create backup. Aborting script."
    exit 1
fi

# ---- Convert package names to directory paths ----
CURRENT_PACKAGE_PATH="${CURRENT_PACKAGE_NAME//./\/}"
NEW_PACKAGE_PATH="${NEW_PACKAGE_NAME//./\/}"

# ---- Move files to new package directory structure ----
log "Finding old package directories..."
while IFS= read -r -d '' dir; do
    if should_skip "$dir"; then
        continue
    fi

    new_dir="${dir//$CURRENT_PACKAGE_PATH/$NEW_PACKAGE_PATH}"

    log "Processing directory: $dir"
    log "New directory: $new_dir"

    if [ ! -d "$dir" ]; then
        log "Directory no longer exists, skipping: $dir"
        continue
    fi

    if [ "$DRY_RUN" = false ]; then
        mkdir -p "$new_dir"
    fi
    log "Created new directory: $new_dir"

    # Move files (not subdirectories) from old to new
    for file in "$dir"/*; do
        [ -f "$file" ] || continue
        new_file_path="$new_dir/$(basename "$file")"
        if [ "$DRY_RUN" = false ]; then
            mv "$file" "$new_file_path"
        fi
        log "Moved file: $file to $new_file_path"
        : $((FILES_MOVED++))
    done

    # Walk up the old directory tree removing empty directories
    base_path="${dir%%$CURRENT_PACKAGE_PATH*}"
    current_dir="$dir"
    while [ "$current_dir" != "$base_path" ] && [ "$current_dir" != "/" ]; do
        if [ -d "$current_dir" ] && [ -z "$(ls -A "$current_dir" 2>/dev/null)" ]; then
            if [ "$DRY_RUN" = false ]; then
                rmdir "$current_dir"
            fi
            log "Removed empty directory: $current_dir"
        else
            if [ -d "$current_dir" ]; then
                log "Directory not empty, stopping removal: $current_dir"
            fi
            break
        fi
        current_dir="$(dirname "$current_dir")"
    done

    : $((DIRS_PROCESSED++))
done < <(find "$ROOT_DIR" -type d -path "*${CURRENT_PACKAGE_PATH}*" -print0 2>/dev/null | sort -z)

# ---- Update file contents ----
log "Processing files for content updates..."

# Build find arguments dynamically from configuration arrays
FIND_SKIP=()
for folder in "${SKIP_FOLDERS[@]}"; do
    if [ ${#FIND_SKIP[@]} -gt 0 ]; then
        FIND_SKIP+=(-o)
    fi
    FIND_SKIP+=(-name "$folder")
done

FIND_EXTS=()
for ext in "${FILE_EXTENSIONS[@]}"; do
    if [ ${#FIND_EXTS[@]} -gt 0 ]; then
        FIND_EXTS+=(-o)
    fi
    FIND_EXTS+=(-name "*.$ext")
done

while IFS= read -r -d '' file; do
    update_file_contents "$file"
done < <(find "$ROOT_DIR" \( "${FIND_SKIP[@]}" \) -prune -o -type f \( "${FIND_EXTS[@]}" \) -print0)

# ---- Delete .git folder if requested ----
if [ "$DELETE_GIT_FOLDER" = true ]; then
    if [ -d "$ROOT_DIR/.git" ]; then
        if [ "$DRY_RUN" = false ]; then
            rm -rf "$ROOT_DIR/.git"
            log "Deleted .git folder"
            echo "Deleted .git folder"
        else
            log "Would delete .git folder (dry run)"
            echo "Would delete .git folder (dry run)"
        fi
    else
        log "No .git folder found"
        echo "No .git folder found"
    fi
else
    log ".git folder was not deleted (use --delete-git flag to delete)"
    echo ".git folder was not deleted (use --delete-git flag to delete)"
fi

# ---- Summary ----
summary="Script Execution Summary:
-------------------------
Package:              $CURRENT_PACKAGE_NAME -> $NEW_PACKAGE_NAME
Display name:         $CURRENT_APP_DISPLAY_NAME -> $NEW_APP_DISPLAY_NAME
Project name:         $CURRENT_PASCAL_CASE_NAME -> $NEW_PASCAL_CASE_NAME
Directories Processed: $DIRS_PROCESSED
Files Moved:          $FILES_MOVED
Files Updated:        $FILES_UPDATED
Dry Run:              $DRY_RUN
.git Folder Deleted:  $DELETE_GIT_FOLDER"

log "$summary"
echo "$summary"

log "Script ended at $(date)"
echo "Package name update process completed. Check the log file at $LOG_FILE for details."
