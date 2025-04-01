#!/bin/bash
#===============================================================================
# TrackAsia Navigation Android Setup Script
# 
# This script helps with:
# 1. Cloning the MapLibre Navigation Android repository
# 2. Renaming all MapLibre references to TrackAsia
# 3. Converting org.trackasia packages to com.trackasia
#===============================================================================

# Global variables
DESTINATION_PATH=""
GITHUB_BASE_URL="https://github.com/maplibre/"
REPO_NAME=""
RNR_REPO="https://github.com/ismaelgv/rnr"

# Advanced search options
SEARCH_CASE_SENSITIVE=false
SEARCH_WHOLE_WORD=false

# Colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print a colored section header
print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# Print a success message
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Print a warning message
print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

# Print an error message
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Clone repositories (MapLibre Navigation Android and RNR tool)
git_clone() {
    print_header "CLONING REPOSITORIES"
    
    # Get GitHub repository name
    read -p "Enter GitHub repository name (default: maplibre-navigation-android): " REPO_NAME
    
    if [ -z "$REPO_NAME" ]; then
        REPO_NAME="maplibre-navigation-android"
        print_warning "Using default repository: $REPO_NAME"
    fi
    
    # Full repository URL
    REPO_URL="${GITHUB_BASE_URL}${REPO_NAME}"
    echo "Will clone from: $REPO_URL"
    
    # Get destination path
    read -p "Enter destination path (press Enter to use current directory): " DESTINATION_PATH

    if [ -z "$DESTINATION_PATH" ]; then
        # Generate folder name based on repository name, but with trackasia prefix
        FOLDER_NAME=$(echo "$REPO_NAME" | sed 's/maplibre/trackasia/g')
        DESTINATION_PATH="$PWD/$FOLDER_NAME"
        print_warning "Using default path: $DESTINATION_PATH"
    fi

    if [ ! -d "$DESTINATION_PATH" ]; then
        mkdir -p "$DESTINATION_PATH"
        print_success "Created directory: $DESTINATION_PATH"
    fi

    echo "Cloning $REPO_NAME repository..."
    git clone --recurse-submodules --branch main "$REPO_URL" "$DESTINATION_PATH"
    
    if [ $? -eq 0 ]; then
        print_success "Successfully cloned repository to $DESTINATION_PATH"
    else
        print_error "Failed to clone repository"
        exit 1
    fi
    
    echo "Cloning RNR string replacement tool..."
    git clone "$RNR_REPO"
    
    if [ $? -eq 0 ]; then
        print_success "Successfully cloned RNR tool"
    else
        print_warning "Failed to clone RNR tool. You may need to install it manually."
    fi
    
    read -p "Press Enter to continue..."
}

# Replace strings using the RNR tool
replace_strings() {
    local from_name="$1"
    local to_name="$2"
    
    echo "Replacing: $from_name -> $to_name"
    
    # Create a temporary file for rnr to exclude trackasia_run_script.sh
    TEMP_EXCLUDE_FILE=$(mktemp)
    echo "trackasia_run_script.sh" > "$TEMP_EXCLUDE_FILE"
    
    # Run RNR with force, recursive, directory options and exclude file
    if rnr -f -r -D -x -X "$TEMP_EXCLUDE_FILE" "$from_name" "$to_name" ./; then
        print_success "Successfully replaced '$from_name' with '$to_name'"
    else
        print_error "Failed to replace '$from_name' with '$to_name'"
        rm -f "$TEMP_EXCLUDE_FILE"
        exit 1
    fi
    
    # Remove temporary file
    rm -f "$TEMP_EXCLUDE_FILE"
}

# Replace strings in specific directories using sed
run_replacement() {
    if [ -z "$1" ] || [ -z "$2" ]; then
        print_error "Usage: run_replacement [find string] [replace string]"
        exit 1
    fi

    local FIND="$1"
    local REPLACE="$2"
    
    # Set locale to ensure consistent behavior with special characters
    export LC_CTYPE=C
    export LANG=C

    # Set default value for DESTINATION_PATH if not provided
    if [ -z "$DESTINATION_PATH" ]; then
        if [ -n "$REPO_NAME" ]; then
            # Generate folder name based on repository name
            FOLDER_NAME=$(echo "$REPO_NAME" | sed 's/maplibre/trackasia/g')
            DESTINATION_PATH="$PWD/$FOLDER_NAME"
        else
            # Fallback to default
            DESTINATION_PATH="$PWD/trackasia-navigation-android"
        fi
        print_warning "Using path: $DESTINATION_PATH"
    fi

    # Check if there's a platform/android directory
    TARGET_DIR="$DESTINATION_PATH/platform/android"
    if [ ! -d "$TARGET_DIR" ]; then
        # Try with just the destination path if platform/android doesn't exist
        TARGET_DIR="$DESTINATION_PATH"
        print_warning "platform/android directory not found, using $TARGET_DIR instead"
    fi
    
    echo "Replacing '$FIND' with '$REPLACE' in $TARGET_DIR"
    echo "Note: trackasia_run_script.sh will be excluded from replacements"
    
    # First, find all files containing the search term
    echo "Searching for files containing '$FIND'..."
    FILES_TO_UPDATE=$(find "$TARGET_DIR" -type f -not -name "trackasia_run_script.sh" -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" -exec grep -l "$FIND" {} \;)
    
    if [ -z "$FILES_TO_UPDATE" ]; then
        print_warning "No files found containing '$FIND'"
        return 0
    fi
    
    # Count number of files to update
    FILE_COUNT=$(echo "$FILES_TO_UPDATE" | wc -l)
    echo "Found $FILE_COUNT files containing '$FIND'"
    
    # Show preview of changes
    echo "Preview of changes:"
    for FILE in $FILES_TO_UPDATE; do
        echo "File: $FILE"
        grep -n "$FIND" "$FILE" | head -n 3
        echo "---"
    done
    
    # Ask for confirmation
    read -p "Do you want to proceed with replacement? (y/N): " CONFIRM
    if [[ $CONFIRM != "y" && $CONFIRM != "Y" ]]; then
        print_warning "Replacement canceled"
        return 0
    fi
    
    # Perform replacement with sed
    for FILE in $FILES_TO_UPDATE; do
        # Create a backup of the original file
        cp "$FILE" "${FILE}.bak"
        
        # Perform the replacement
        sed -i '' "s|${FIND}|${REPLACE}|g" "$FILE"
        
        # Check if the replacement was successful
        if grep -q "$FIND" "$FILE"; then
            print_error "Failed to replace all instances in $FILE"
            # Restore from backup
            mv "${FILE}.bak" "$FILE"
        else
            print_success "Successfully updated $FILE"
            rm "${FILE}.bak"
        fi
    done
    
    print_success "Replacement completed"
}

# Run all string replacements
run_all_replacements() {
    print_header "RUNNING ALL STRING REPLACEMENTS"
    
    # Array of replacements [from, to]
    declare -a REPLACEMENTS=(
        "MAPLIBRE" "TRACKASIA"
        "MapLibre" "TrackAsia"
        "Maplibre" "Trackasia"
        "mapLibre" "trackAsia"
        "maplibre.org" "track-asia.com"
        "/maplibre/" "/trackasia/"
        "maplibre/" "trackasia/"
        "/maplibre" "/trackasia"
        "maplibre-" "trackasia-"
        "maplibre_" "trackasia_"
        "org.maplibre" "com.trackasia"
        "com.trackasia.gl" "io.github.track-asia"
        "maplibre" "trackasia"
        "org.trackasia" "com.trackasia"
    )
    
    echo "Note: trackasia_run_script.sh will be excluded from replacements"
    
    # Run all replacements using RNR tool
    for ((i=0; i<${#REPLACEMENTS[@]}; i+=2)); do
        replace_strings "${REPLACEMENTS[i]}" "${REPLACEMENTS[i+1]}"
    done
    
    # Replace the demo tiles URL
    run_replacement "https://demotiles.maplibre.org/style.json" "https://maps.track-asia.com/styles/v1/streets.json?key=public_key"
    
    # Replace the versatiles URL
    run_replacement "https://tiles.versatiles.org/assets/styles/colorful/style.json" "https://maps.track-asia.com/styles/v1/streets.json?key=public_key"
    
    print_success "All string replacements completed"
}

# Rename Java/Kotlin packages from org.trackasia to com.trackasia
rename_java_packages() {
    print_header "RENAMING JAVA/KOTLIN PACKAGES"
    
    # Set default value for DESTINATION_PATH if not provided
    if [ -z "$DESTINATION_PATH" ]; then
        if [ -n "$REPO_NAME" ]; then
            # Generate folder name based on repository name
            FOLDER_NAME=$(echo "$REPO_NAME" | sed 's/maplibre/trackasia/g')
            DESTINATION_PATH="$PWD/$FOLDER_NAME"
        else
            # Fallback to default
            DESTINATION_PATH="$PWD/trackasia-navigation-android"
        fi
        print_warning "Using path: $DESTINATION_PATH"
    fi
    
    # Change to destination directory for operations
    cd "$DESTINATION_PATH" || {
        print_error "Failed to navigate to $DESTINATION_PATH"
        return 1
    }
    
    echo "Searching for all directories with path org/trackasia..."

    # Find all org/trackasia directories in the project
    FOLDERS=$(find . -type d -path "*/org/trackasia")

    if [ -z "$FOLDERS" ]; then
        print_warning "No org/trackasia directories found! You may need to run string replacements first."
        return 0
    fi

    for FOLDER in $FOLDERS; do
        NEW_FOLDER=$(echo "$FOLDER" | sed 's|/org/trackasia|/com/trackasia|g')
        
        echo "Converting: $FOLDER → $NEW_FOLDER"

        # Create new directory if it doesn't exist
        mkdir -p "$NEW_FOLDER"

        # Move all files from org/trackasia to com/trackasia
        mv "$FOLDER"/* "$NEW_FOLDER/" 2>/dev/null || print_warning "No files to move in $FOLDER"
    done

    # Now delete all the org directories after all files have been moved
    echo "Removing org directories..."
    
    # First find org/trackasia directories
    find . -type d -path "*/org/trackasia" | while read -r DIR; do
        echo "Removing directory: $DIR"
        rm -rf "$DIR"
    done
    
    # Then find any empty org directories (parents of org/trackasia)
    find . -type d -name "org" | while read -r DIR; do
        # Only delete if it's empty
        if [ -z "$(ls -A "$DIR" 2>/dev/null)" ]; then
            echo "Removing empty org directory: $DIR"
            rmdir "$DIR" 2>/dev/null
        else
            echo "Skipping non-empty org directory: $DIR"
        fi
    done

    print_success "Directory renaming completed"

    echo "Replacing org.trackasia → com.trackasia in all files..."

    # List of file extensions to process
    FILE_TYPES=("java" "kt" "xml" "gradle" "properties" "json")

    for EXT in "${FILE_TYPES[@]}"; do
        echo "Processing *.${EXT} files..."
        find . -type f -name "*.${EXT}" -exec sed -i '' 's/org\.trackasia/com.trackasia/g' {} \; 2>/dev/null
    done

    # Return to original directory
    cd - >/dev/null || print_warning "Failed to return to original directory"

    print_success "Content replacement completed in all files"
}

# Advanced search and replace function similar to VS Code
advanced_search_replace() {
    print_header "ADVANCED SEARCH AND REPLACE"
    
    # Ask if user wants to use predefined replacements
    read -p "Use predefined replacements list? (y/N): " USE_PREDEFINED
    
    if [[ $USE_PREDEFINED == "y" || $USE_PREDEFINED == "Y" ]]; then
        # Show the predefined replacements
        print_header "PREDEFINED REPLACEMENTS"
        echo "The following replacements will be performed:"
        
        # Array of replacements [from, to]
        declare -a REPLACEMENTS=(
            "MAPLIBRE" "TRACKASIA"
            "MapLibre" "TrackAsia"
            "Maplibre" "Trackasia"
            "mapLibre" "trackAsia"
            "maplibre.org" "track-asia.com"
            "/maplibre/" "/trackasia/"
            "maplibre/" "trackasia/"
            "/maplibre" "/trackasia"
            "maplibre-" "trackasia-"
            "maplibre_" "trackasia_"
            "org.maplibre" "com.trackasia"
            "com.trackasia.gl" "io.github.track-asia"
            "maplibre" "trackasia"
            "org.trackasia" "com.trackasia"
        )
        
        # Display replacements in a formatted way
        for ((i=0; i<${#REPLACEMENTS[@]}; i+=2)); do
            INDEX=$((i/2+1))
            echo "$INDEX) \"${REPLACEMENTS[i]}\" → \"${REPLACEMENTS[i+1]}\""
        done
        
        echo ""
        echo "Additionally, the following URL will be replaced:"
        echo "\"https://demotiles.maplibre.org/style.json\" → \"https://maps.track-asia.com/styles/v1/streets.json?key=public_key\""
        
        # Get directory to search in
        if [ -z "$DESTINATION_PATH" ] || [ ! -d "$DESTINATION_PATH" ]; then
            # Prompt for directory to search in
            read -p "Enter path to search in (press Enter to use current directory): " SEARCH_PATH
            if [ -z "$SEARCH_PATH" ]; then
                SEARCH_PATH="$PWD"
            fi
            
            if [ ! -d "$SEARCH_PATH" ]; then
                print_error "Directory does not exist: $SEARCH_PATH"
                return 1
            fi
        else
            SEARCH_PATH="$DESTINATION_PATH"
        fi
        
        # File type filter
        read -p "File extensions to search (e.g., java,kt,xml or leave empty for all files): " FILE_EXTENSIONS
        
        # Confirm before proceeding
        read -p "Do you want to proceed with all replacements? (y/N): " CONFIRM
        if [[ $CONFIRM != "y" && $CONFIRM != "Y" ]]; then
            print_warning "Replacements canceled"
            return 0
        fi
        
        # Change to search directory
        cd "$SEARCH_PATH" || {
            print_error "Failed to navigate to $SEARCH_PATH"
            return 1
        }
        
        print_header "PERFORMING REPLACEMENTS"
        echo "Processing files in: $SEARCH_PATH"
        echo "Note: trackasia_run_script.sh will be excluded from replacements"
        
        # Process each replacement pair
        for ((i=0; i<${#REPLACEMENTS[@]}; i+=2)); do
            echo "Replacing: \"${REPLACEMENTS[i]}\" → \"${REPLACEMENTS[i+1]}\""
            
            if [ -n "$FILE_EXTENSIONS" ]; then
                # Process specific file types
                EXTENSIONS_ARRAY=(${FILE_EXTENSIONS//,/ })
                for EXT in "${EXTENSIONS_ARRAY[@]}"; do
                    echo "  Processing *.$EXT files..."
                    find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" \
                         -exec sed -i '' "s|${REPLACEMENTS[i]}|${REPLACEMENTS[i+1]}|g" {} \; 2>/dev/null
                done
            else
                # Process all non-binary files
                find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
                     -not -name "trackasia_run_script.sh" \
                     -exec sed -i '' "s|${REPLACEMENTS[i]}|${REPLACEMENTS[i+1]}|g" {} \; 2>/dev/null
            fi
        done
        
        # Replace the demo tiles URL
        echo "Replacing URL: \"https://demotiles.maplibre.org/style.json\" → \"https://maps.track-asia.com/styles/v1/streets.json?key=public_key\""
        if [ -n "$FILE_EXTENSIONS" ]; then
            # Process specific file types
            for EXT in "${EXTENSIONS_ARRAY[@]}"; do
                find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" \
                     -exec sed -i '' "s|https://demotiles.maplibre.org/style.json|https://maps.track-asia.com/styles/v1/streets.json?key=public_key|g" {} \; 2>/dev/null
            done
        else
            # Process all non-binary files
            find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
                 -not -name "trackasia_run_script.sh" \
                 -exec sed -i '' "s|https://demotiles.maplibre.org/style.json|https://maps.track-asia.com/styles/v1/streets.json?key=public_key|g" {} \; 2>/dev/null
        fi
        
        # Return to original directory if necessary
        if [ "$SEARCH_PATH" != "$PWD" ]; then
            cd - >/dev/null || print_warning "Failed to return to original directory"
        fi
        
        print_success "All predefined replacements completed"
        return 0
    fi
    
    # Manual search and replace
    if [ -z "$DESTINATION_PATH" ] || [ ! -d "$DESTINATION_PATH" ]; then
        # Prompt for directory to search in
        read -p "Enter path to search in (press Enter to use current directory): " SEARCH_PATH
        if [ -z "$SEARCH_PATH" ]; then
            SEARCH_PATH="$PWD"
        fi
        
        if [ ! -d "$SEARCH_PATH" ]; then
            print_error "Directory does not exist: $SEARCH_PATH"
            return 1
        fi
    else
        SEARCH_PATH="$DESTINATION_PATH"
    fi
    
    # Change to search directory
    cd "$SEARCH_PATH" || {
        print_error "Failed to navigate to $SEARCH_PATH"
        return 1
    }
    
    # Get search term
    read -p "Enter text to search for: " SEARCH_TERM
    if [ -z "$SEARCH_TERM" ]; then
        print_error "Search term cannot be empty"
        return 1
    fi
    
    # Get replacement text
    read -p "Enter replacement text: " REPLACE_TERM
    if [ -z "$REPLACE_TERM" ]; then
        print_warning "Using empty string as replacement"
    fi
    
    # Search options
    read -p "Case sensitive search? (y/N): " CASE_SENSITIVE
    if [[ $CASE_SENSITIVE == "y" || $CASE_SENSITIVE == "Y" ]]; then
        SEARCH_CASE_SENSITIVE=true
        GREP_CASE_OPT=""
        SED_CASE_OPT=""
    else
        SEARCH_CASE_SENSITIVE=false
        GREP_CASE_OPT="-i"
        SED_CASE_OPT="I"
    fi
    
    # File type filter
    read -p "File extensions to search (e.g., java,kt,xml or leave empty for all files): " FILE_EXTENSIONS
    if [ -n "$FILE_EXTENSIONS" ]; then
        # Convert comma-separated list to find format
        FILE_PATTERN=$(echo "$FILE_EXTENSIONS" | sed 's/,/\\|/g')
        FILE_FILTER="-name \"*.$FILE_PATTERN\""
    else
        # Default: exclude common binary and hidden files
        FILE_FILTER="-type f -not -path \"*/\.*\" -not -path \"*/build/*\" -not -path \"*/bin/*\""
    fi
    
    echo "Note: trackasia_run_script.sh will be excluded from search and replacements"
    
    # First preview the changes
    print_header "PREVIEWING CHANGES"
    echo "Searching for '$SEARCH_TERM' in files..."
    
    # Prepare the find command based on file filter
    if [ -n "$FILE_EXTENSIONS" ]; then
        # Use the file extension pattern
        EXTENSIONS_ARRAY=(${FILE_EXTENSIONS//,/ })
        for EXT in "${EXTENSIONS_ARRAY[@]}"; do
            find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" -print0 | 
            xargs -0 grep $GREP_CASE_OPT --color=always -n "$SEARCH_TERM" 2>/dev/null
        done
    else
        # Search in all non-binary files
        find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
             -not -name "trackasia_run_script.sh" -print0 | 
        xargs -0 grep $GREP_CASE_OPT --color=always -n "$SEARCH_TERM" 2>/dev/null
    fi
    
    # Count matches
    if [ -n "$FILE_EXTENSIONS" ]; then
        # Count based on file extensions
        MATCH_COUNT=0
        for EXT in "${EXTENSIONS_ARRAY[@]}"; do
            COUNT=$(find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" -print0 | 
                   xargs -0 grep $GREP_CASE_OPT -l "$SEARCH_TERM" 2>/dev/null | wc -l)
            MATCH_COUNT=$((MATCH_COUNT + COUNT))
        done
    else
        # Count for all files
        MATCH_COUNT=$(find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
                    -not -name "trackasia_run_script.sh" -print0 | 
                     xargs -0 grep $GREP_CASE_OPT -l "$SEARCH_TERM" 2>/dev/null | wc -l)
    fi
    
    echo ""
    echo "Found $MATCH_COUNT files containing '$SEARCH_TERM'"
    echo ""
    
    # Confirm replacement
    read -p "Do you want to proceed with replacement? (y/N): " CONFIRM
    if [[ $CONFIRM != "y" && $CONFIRM != "Y" ]]; then
        print_warning "Replacement canceled"
        return 0
    fi
    
    print_header "PERFORMING REPLACEMENTS"
    echo "Replacing '$SEARCH_TERM' with '$REPLACE_TERM'..."
    
    # Counter for replaced files
    REPLACED_FILES=0
    
    # Perform the replacement based on the file filter
    if [ -n "$FILE_EXTENSIONS" ]; then
        # Replace in specific file types
        for EXT in "${EXTENSIONS_ARRAY[@]}"; do
            echo "Processing *.$EXT files..."
            if [ "$SEARCH_CASE_SENSITIVE" = true ]; then
                find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" \
                     -exec sed -i '' "s|$SEARCH_TERM|$REPLACE_TERM|g" {} \; 2>/dev/null
            else
                find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" \
                     -exec sed -i '' "s|$SEARCH_TERM|$REPLACE_TERM|g$SED_CASE_OPT" {} \; 2>/dev/null
            fi
            # Count replacements for this extension
            COUNT=$(find . -type f -name "*.$EXT" -not -name "trackasia_run_script.sh" -print0 | 
                   xargs -0 grep $GREP_CASE_OPT -l "$SEARCH_TERM" 2>/dev/null | wc -l)
            REPLACED_FILES=$((REPLACED_FILES + COUNT))
        done
    else
        # Replace in all non-binary files
        echo "Processing all suitable files..."
        if [ "$SEARCH_CASE_SENSITIVE" = true ]; then
            find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
                 -not -name "trackasia_run_script.sh" \
                 -exec sed -i '' "s|$SEARCH_TERM|$REPLACE_TERM|g" {} \; 2>/dev/null
        else
            find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/bin/*" \
                 -not -name "trackasia_run_script.sh" \
                 -exec sed -i '' "s|$SEARCH_TERM|$REPLACE_TERM|g$SED_CASE_OPT" {} \; 2>/dev/null
        fi
        # Count of files processed
        REPLACED_FILES=$MATCH_COUNT
    fi
    
    # Return to original directory if necessary
    if [ "$SEARCH_PATH" != "$PWD" ]; then
        cd - >/dev/null || print_warning "Failed to return to original directory"
    fi
    
    print_success "Replaced '$SEARCH_TERM' with '$REPLACE_TERM' in $REPLACED_FILES files"
}

# Main function
main() {
    print_header "TRACKASIA NAVIGATION ANDROID SETUP"
    echo "This script helps set up TrackAsia Navigation Android from MapLibre"
    echo "You can clone any MapLibre repository and convert it to TrackAsia"
    echo "Select an option to proceed:"
    
    options=(
        "Clone Repository" 
        "Run String Replacements" 
        "Rename Java/Kotlin Packages (org/trackasia -> com/trackasia)"
        "Advanced Search and Replace" 
        "Run All Steps (Clone + Replace + Rename)"
        "Exit"
    )
    
    select opt in "${options[@]}"; do
        case $opt in
            "Clone Repository")
                git_clone
                ;;
            "Run String Replacements")
                run_all_replacements
                ;;
            "Rename Java/Kotlin Packages (org/trackasia -> com/trackasia)")
                rename_java_packages
                ;;
            "Advanced Search and Replace")
                advanced_search_replace
                ;;
            "Run All Steps (Clone + Replace + Rename)")
                git_clone
                if [ -d "$DESTINATION_PATH" ]; then
                    # Change to destination directory for replacements
                    cd "$DESTINATION_PATH" || {
                        print_error "Failed to navigate to $DESTINATION_PATH"
                        break
                    }
                    
                    run_all_replacements
                    
                    # Return to original directory 
                    cd - >/dev/null || print_warning "Failed to return to original directory"
                    
                    # Now run the rename function which handles its own directory changes
                    rename_java_packages
                    
                    print_success "All steps completed successfully"
                else
                    print_error "Repository clone failed, cannot proceed with other steps"
                fi
                ;;
            "Exit")
                echo "Exiting script"
                exit 0
                ;;
            *) 
                print_error "Invalid option"
                ;;
        esac
        
        # Return to menu after completing a task
        print_header "TRACKASIA NAVIGATION ANDROID SETUP"
        echo "Select another option or Exit:"
    done
}

# Call the main function
main
