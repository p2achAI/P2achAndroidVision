#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${1:-P2achAndroidVision}"
REPO_URL="${REPO_URL:-git@github.com:p2achAI/P2achAndroidVision.git}"
SHALLOW="${SHALLOW:-false}"

SUB_PATHS=("commonLibrary" "libuvccamera" "app/src/main/cpp")
SUB_URLS=(
  "git@github.com:p2achAI/commonLibrary.git"
  "git@github.com:p2achAI/libuvccamera-only.git"
  "git@github.com:p2achAI/p2ach_vision_cpp.git"
)

detect_default_branch() {
  local url="$1"
  local def
  def="$(git ls-remote --symref "$url" HEAD 2>/dev/null \
     | awk '/^ref:/ { sub("refs/heads/","",$2); print $2; exit }' || true)"
  [[ -z "${def:-}" ]] && def="main"
  echo "$def"
}

echo "=== Step 0: Download OpenCV ==="

TMP_OPENCV_DIR=".tmp_opencv"

rm -rf "$TMP_OPENCV_DIR"
mkdir -p "$TMP_OPENCV_DIR"

OPENCV_ZIP="$TMP_OPENCV_DIR/opencv.zip"

curl -L \
  -o "$OPENCV_ZIP" \
  "https://github.com/opencv/opencv/releases/download/4.9.0/opencv-4.9.0-android-sdk.zip"

unzip -q "$OPENCV_ZIP" -d "$TMP_OPENCV_DIR"

OPENCV_SDK_DIR=$(find "$TMP_OPENCV_DIR" -type d -name "sdk" | head -1)

if [[ -z "$OPENCV_SDK_DIR" ]]; then
  echo "[ERROR] Failed to locate OpenCV sdk folder"
  exit 1
fi

echo "=== Step 1: Clone Main Repo ==="

if [[ ! -d "$ROOT_DIR" ]]; then
  if [[ "$SHALLOW" == "true" ]]; then
    git clone --depth 1 --progress "$REPO_URL" "$ROOT_DIR"
  else
    git clone --progress "$REPO_URL" "$ROOT_DIR"
  fi
else
  echo "[skip] $ROOT_DIR already exists"
fi

# Now sdk folder can be moved safely
echo "=== Step 2: Move OpenCV sdk into project ==="
rm -rf "$ROOT_DIR/sdk"
mv "$OPENCV_SDK_DIR" "$ROOT_DIR/sdk"

echo "OpenCV sdk → $ROOT_DIR/sdk"

cd "$ROOT_DIR"

echo "=== Step 3: Insert :sdk into settings.gradle ==="

if [[ -f "settings.gradle.kts" ]]; then
  SETTINGS="settings.gradle.kts"
elif [[ -f "settings.gradle" ]]; then
  SETTINGS="settings.gradle"
else
  echo "[ERROR] settings.gradle(.kts) not found"
  exit 1
fi

if ! grep -q 'include(":sdk")' "$SETTINGS"; then
cat << 'EOF' >> "$SETTINGS"

include(":sdk")
project(":sdk").projectDir = file("sdk")
EOF
fi

echo "=== Step 4: Patch sdk build.gradle ==="

if [[ -f "sdk/build.gradle" ]]; then
cat << 'EOF' >> sdk/build.gradle

android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}
EOF
fi

echo "=== Step 5: Setup submodules ==="

for i in "${!SUB_PATHS[@]}"; do
  path="${SUB_PATHS[$i]}"
  url="${SUB_URLS[$i]}"
  br="$(detect_default_branch "$url")"

  echo " - $path ($br)"

  if git config -f .gitmodules --get "submodule.${path}.url" >/dev/null 2>&1; then
    git submodule set-url "$path" "$url"
    git submodule set-branch --branch "$br" "$path" >/dev/null 2>&1 || true
  else
    [[ -d "$path" && ! -d "$path/.git" ]] && rm -rf "$path"
    git submodule add -b "$br" "$url" "$path"
  fi
done

git submodule sync --recursive

if [[ "$SHALLOW" == "true" ]]; then
  git submodule update --init --recursive --depth 1 --progress
else
  git submodule update --init --recursive --progress
fi

echo "=== DONE ==="