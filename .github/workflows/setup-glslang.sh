#!/bin/bash
set -e

# Variables
GLSLANG_ZIP_URL="https://github.com/KhronosGroup/glslang/releases/download/master-tot/glslang-master-windows-x64-Release.zip"
GLSLANG_DIR="${GITHUB_WORKSPACE}/glslang"
GLSLANG_ZIP="${GITHUB_WORKSPACE}/glslang.zip"
GLSLANG_VALIDATOR="${GLSLANG_DIR}/bin/glslangValidator.exe"

# Download glslang
echo "Downloading glslang from ${GLSLANG_ZIP_URL}..."
curl -L -o ${GLSLANG_ZIP} ${GLSLANG_ZIP_URL}

# Extract glslang
echo "Extracting glslang..."
mkdir -p ${GLSLANG_DIR}
unzip -q ${GLSLANG_ZIP} -d ${GLSLANG_DIR}

# Verify glslangValidator installation
echo "Verifying glslangValidator installation..."
if [ -f "${GLSLANG_VALIDATOR}" ]; then
    echo "glslangValidator is installed successfully."
    ${GLSLANG_VALIDATOR} --version
else
    echo "Error: glslangValidator could not be found." >&2
    exit 1
fi

# Update PATH
echo "Adding glslang to PATH..."
echo "${GLSLANG_DIR}/bin" >> $GITHUB_PATH

echo "glslang setup completed successfully."
