#!/usr/bin/env bash
set -euo pipefail

# ===== 사용자 설정 =====
ROOT_DIR="${1:-P2achAndroidLibrary}"
REPO_URL="${REPO_URL:-git@github.com:p2achAI/P2achAndroidLibrary.git}"
# 히스토리 보존(기본) / 빠른 얕은 클론 선택
SHALLOW="${SHALLOW:-false}"   # true | false

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

echo "=== P2achAndroidLibrary setup (SHALLOW=$SHALLOW) ==="

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

# 1) 서브모듈 등록/정합성 (절대 삭제 안 함)
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
    # 잔존 디렉터리가 깃관리 아니면 제거
    if [[ -d "$path" && ! -d "$path/.git" ]]; then
      echo "    > cleanup leftover dir: $path"
      rm -rf "$path"
    fi
    git submodule add -b "$br" "$url" "$path"
  fi
done

# 2) 서브모듈 sync & checkout (blob:none 금지: 빈 폴더 오해 방지)
echo "[step] submodule sync/update"
git submodule sync --recursive
if [[ "$SHALLOW" == "true" ]]; then
  git submodule update --init --recursive --depth 1 --recommend-shallow --progress
else
  git submodule update --init --recursive --progress
fi

# 3) 최신 반영(기본 브랜치로 fast-forward)
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