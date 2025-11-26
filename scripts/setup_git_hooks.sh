#!/bin/bash

HOOK_DIR=".git/hooks"
HOOK_SRC_DIR="scripts/hooks"

cp ${HOOK_SRC_DIR}/post-checkout ${HOOK_DIR}/post-checkout
chmod +x ${HOOK_DIR}/post-checkout
echo "[hook setup] post-checkout hook installed"

cp ${HOOK_SRC_DIR}/post-merge ${HOOK_DIR}/post-merge
chmod +x ${HOOK_DIR}/post-merge
echo "[hook setup] post-merge hook installed"