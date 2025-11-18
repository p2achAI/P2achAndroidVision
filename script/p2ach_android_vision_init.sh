#!/usr/bin/env bash
set -euo pipefail

# ===== 사용자 설정 =====
ROOT_DIR="${1:-P2achAndroidVision}"
REPO_URL="${REPO_URL:-git@github.com:p2achAI/P2achAndroidVision.git}"
SHALLOW="${SHALLOW:-false}"   # true | false

# 서브모듈 목록(동일 인덱스 매칭)
SUB_PATHS=(
  "commonLibrary"
  "libuvccamera"
  "app/src/main/cpp"
)
SUB_URLS=(
  "git@github.com:p2achAI/commonLibrary.git"
  "git@github.com:p2achAI/libuvccamera-only.git"
  "git@github.com:p2achAI/p2ach_vision_cpp.git"
)

detect_default_branch() {
  local url="$1"
  local def
  def="$(git ls-remote --symref "$url" HEAD 2>/dev/null \
        | awk '/^ref:/ { sub("refs/heads/","",$2); print $2; exit }' \
        || true)"
  [[ -z "${def:-}" ]] && def="main"
  echo "$def"
}

echo "=== P2achAndroidVision setup (SHALLOW=$SHALLOW) ==="

echo "[step] clone main repo (without submodules) -> $ROOT_DIR"
if [[ ! -d "$ROOT_DIR/.git" ]]; then
  if [[ "$SHALLOW" == "true" ]]; then
    git clone --progress --depth 1 "$REPO_URL" "$ROOT_DIR"
  else
    git clone --progress "$REPO_URL" "$ROOT_DIR"
  fi
else
  echo "  - skip: repo already exists"
fi

cd "$ROOT_DIR"

echo "[step] ensure .gitmodules entries"
for i in "${!SUB_PATHS[@]}"; do
  path="${SUB_PATHS[$i]}"
  url="${SUB_URLS[$i]}"
  br="$(detect_default_branch "$url")"
  echo "  - $path (url=$url, branch=$br)"

  if git config -f .gitmodules --get "submodule.${path}.url" >/dev/null 2>&1; then
    cur_url="$(git config -f .gitmodules --get "submodule.${path}.url" || true)"
    if [[ "$cur_url" != "$url" ]]; then
      echo "    > update url: $cur_url -> $url"
      git config -f .gitmodules "submodule.${path}.url" "$url"
    fi
    git config -f .gitmodules "submodule.${path}.branch" "$br" >/dev/null 2>&1 || true
  else
    echo "    > add submodule entry to .gitmodules"
    git submodule add -f -b "$br" "$url" "$path" || true
  fi
done

echo "[step] sync .gitmodules to local config"
git submodule sync --recursive || true

echo "[step] materialize submodules (ignore stored SHA, always use latest branch)"
for i in "${!SUB_PATHS[@]}"; do
  path="${SUB_PATHS[$i]}"
  url="${SUB_URLS[$i]}"
  br="$(git config -f .gitmodules --get "submodule.${path}.branch" || detect_default_branch "$url")"

  echo "  - prepare $path (branch=$br)"

  if [[ -d "$path" && ! -d "$path/.git" ]]; then
    echo "    > leftover non-git dir found, removing: $path"
    rm -rf "$path"
  fi

  if [[ ! -d "$path/.git" ]]; then
    echo "    > fresh clone into $path"
    if [[ "$SHALLOW" == "true" ]]; then
      git clone --progress --depth 1 --branch "$br" "$url" "$path"
    else
      git clone --progress --branch "$br" "$url" "$path"
    fi
  else
    echo "    > existing git repo, fetch & reset"
    if [[ "$SHALLOW" == "true" ]]; then
      ( cd "$path" && git fetch --depth 1 origin "$br" || true )
    else
      ( cd "$path" && git fetch origin "$br" || true )
    fi
  fi

  echo "    > checkout origin/$br (ignore superproject SHA)"
  ( cd "$path" && git checkout -B "$br" "origin/$br" || true )

  # superproject 입장에서는 이 서브모듈이 "새 커밋" 상태일 수 있지만,
  # 빌드는 정상 동작. 필요하면 나중에 git add/commit 로 고정.
done

echo "[done] submodule status:"
git submodule status --recursive || true