#!/usr/bin/env bash
set -euo pipefail

# ===== 사용자 설정 =====
ROOT_DIR="${1:-P2achAndroidVision}"
REPO_URL="${REPO_URL:-git@github.com:p2achAI/P2achAndroidVision.git}"
SHALLOW="${SHALLOW:-false}"   # true | false

# OpenCV Android SDK 4.9.0
OPENCV_URL="https://github.com/opencv/opencv/releases/download/4.9.0/opencv-4.9.0-android-sdk.zip"
OPENCV_ZIP="opencv-4.9.0-android-sdk.zip"
OPENCV_DIR="OpenCV-android-sdk"   # zip 안에서 나오는 디렉터리 이름
OPENCV_MODULE_NAME="sdk"          # Gradle 모듈명 :sdk

# 서브모듈 목록(동일 인덱스 매칭)
SUB_PATHS=( "commonLibrary" "libuvccamera" )
SUB_URLS=(  "git@github.com:p2achAI/commonLibrary.git"
            "git@github.com:p2achAI/libuvccamera-only.git" )

detect_default_branch() {
  local url="$1"
  local def
  def="$(git ls-remote --symref "$url" HEAD 2>/dev/null | awk '/^ref:/ { sub("refs/heads/","",$2); print $2; exit }' || true)"
  [[ -z "${def:-}" ]] && def="main"
  echo "$def"
}

echo "=== P2achAndroidVision setup (SHALLOW=$SHALLOW) ==="

echo "[step] download OpenCV Android SDK (${OPENCV_URL})"
if [[ ! -f "$OPENCV_ZIP" ]]; then
  curl -L -o "$OPENCV_ZIP" "$OPENCV_URL"
else
  echo "  - zip already exists: $OPENCV_ZIP (skip download)"
fi

if [[ ! -d "$OPENCV_DIR" ]]; then
  echo "[step] unzip OpenCV SDK -> $OPENCV_DIR"
  unzip -q "$OPENCV_ZIP"
else
  echo "[skip] OpenCV dir already exists: $OPENCV_DIR"
fi

# 0) 메인 레포 클론
if [[ ! -d "$ROOT_DIR/.git" ]]; then
  echo "[step] clone main repo -> $ROOT_DIR"
  if [[ "$SHALLOW" == "true" ]]; then
    git clone --progress --depth 1 "$REPO_URL" "$ROOT_DIR"
  else
    git clone --progress "$REPO_URL" "$ROOT_DIR"
  fi
else
  echo "[skip] already exists: $ROOT_DIR"
fi

cd "$ROOT_DIR"

# 1) 서브모듈 등록/정합성
echo "[step] submodule ensure/add"
for i in "${!SUB_PATHS[@]}"; do
  path="${SUB_PATHS[$i]}"
  url="${SUB_URLS[$i]}"
  br="$(detect_default_branch "$url")"
  echo "  - $path (url=$url, branch=$br)"

  if git config -f .gitmodules --get "submodule.${path}.url" >/dev/null 2>&1; then
    cur_url="$(git config -f .gitmodules --get "submodule.${path}.url" || true)"
    [[ "$cur_url" != "$url" ]] && git submodule set-url "$path" "$url"
    git submodule set-branch --branch "$br" "$path" >/dev/null 2>&1 || true
  else
    if [[ -d "$path" && ! -d "$path/.git" ]]; then
      echo "    > cleanup leftover dir: $path"
      rm -rf "$path"
    fi
    git submodule add -b "$br" "$url" "$path"
  fi
done

echo "[step] submodule sync/update"
git submodule sync --recursive
if [[ "$SHALLOW" == "true" ]]; then
  git submodule update --init --recursive --depth 1 --recommend-shallow --progress
else
  git submodule update --init --recursive --progress
fi

echo "[step] submodule fast-forward to default branch"
for i in "${!SUB_PATHS[@]}"; do
  path="${SUB_PATHS[$i]}"
  url="${SUB_URLS[$i]}"
  br="$(git config -f .gitmodules --get "submodule.${path}.branch" || detect_default_branch "$url")"
  echo "  - $path -> $br"
  if [[ "$SHALLOW" == "true" ]]; then
    (cd "$path" && git fetch --depth 1 origin "$br" && git checkout -q "$br" && git pull --ff-only)
  else
    (cd "$path" && git fetch origin "$br" && git checkout -q "$br" && git pull --ff-only)
  fi
done

echo "[done] submodule status:"
git submodule status --recursive || true

# 4) OpenCV를 :sdk 모듈로 추가
echo "[step] install OpenCV as :sdk module"

if [[ -d "../${OPENCV_DIR}/sdk" ]]; then
  if [[ ! -d "${OPENCV_MODULE_NAME}" ]]; then
    echo "  - copy ../${OPENCV_DIR}/sdk -> ${OPENCV_MODULE_NAME}"
    cp -R "../${OPENCV_DIR}/sdk" "${OPENCV_MODULE_NAME}"
  else
    echo "  - ${OPENCV_MODULE_NAME} directory already exists (skip copy)"
  fi
else
  echo "  [warn] ../${OPENCV_DIR}/sdk not found. Check unzip path."
fi

SETTINGS_FILE=""
if [[ -f "settings.gradle.kts" ]]; then
  SETTINGS_FILE="settings.gradle.kts"
elif [[ -f "settings.gradle" ]]; then
  SETTINGS_FILE="settings.gradle"
fi

if [[ -n "$SETTINGS_FILE" ]]; then
  echo "  - update $SETTINGS_FILE for :${OPENCV_MODULE_NAME}"
  if ! grep -q '":sdk"' "$SETTINGS_FILE"; then
    cat <<EOF >> "$SETTINGS_FILE"

include(":sdk")
project(":sdk").projectDir = file("sdk")
EOF
  else
    echo "    > :sdk already declared in $SETTINGS_FILE (skip)"
  fi
else
  echo "  [warn] settings.gradle(.kts) not found. Please add :sdk manually."
fi

echo "[all done] P2achAndroidVision + OpenCV :sdk module ready."