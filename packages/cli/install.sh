#!/bin/sh
# Installs feedctl, the Feedless CLI, from the instance that serves this
# script. Downloading from the instance guarantees the CLI build matches
# that instance's API version -- there is no CLI/server compatibility
# matrix to maintain.
#
# Usage:
#   curl -fsSL <instance>/cli/install.sh | sh
#   curl -fsSL <instance>/cli/install.sh | sh -s -- --dir /custom/path
#
# FEEDCTL_BASE_URL is baked in by the instance that serves this script. Set
# it explicitly to run this script against a plain file server (e.g. for
# local testing) that has not substituted it.
set -eu

FEEDCTL_BASE_URL="${FEEDCTL_BASE_URL:-__FEEDCTL_BASE_URL__}"
INSTALL_DIR="${HOME}/.local/bin"

while [ "$#" -gt 0 ]; do
  case "$1" in
    --dir)
      if [ "$#" -lt 2 ]; then
        echo "feedctl install: --dir requires a value" >&2
        exit 1
      fi
      INSTALL_DIR="$2"
      shift 2
      ;;
    --dir=*)
      INSTALL_DIR="${1#--dir=}"
      shift
      ;;
    *)
      echo "feedctl install: unknown argument '$1'" >&2
      exit 1
      ;;
  esac
done

if [ "${FEEDCTL_BASE_URL}" = "__FEEDCTL_BASE_URL__" ]; then
  echo "feedctl install: FEEDCTL_BASE_URL is not set. Run this script from" >&2
  echo "an instance's /cli/install.sh, or set FEEDCTL_BASE_URL explicitly." >&2
  exit 1
fi

os="$(uname -s)"
case "${os}" in
  Darwin) os="darwin" ;;
  Linux) os="linux" ;;
  *)
    echo "feedctl install: unsupported OS '${os}'" >&2
    exit 1
    ;;
esac

arch="$(uname -m)"
case "${arch}" in
  x86_64 | amd64) arch="amd64" ;;
  arm64 | aarch64) arch="arm64" ;;
  *)
    echo "feedctl install: unsupported architecture '${arch}'" >&2
    exit 1
    ;;
esac

binary="feedctl-${os}-${arch}"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

echo "feedctl install: downloading ${binary} from ${FEEDCTL_BASE_URL}" >&2
curl -fsSL -o "${work_dir}/${binary}" "${FEEDCTL_BASE_URL}/cli/${binary}"
curl -fsSL -o "${work_dir}/SHA256SUMS" "${FEEDCTL_BASE_URL}/cli/SHA256SUMS"

checksum_line="$(grep "  ${binary}\$" "${work_dir}/SHA256SUMS" || true)"
if [ -z "${checksum_line}" ]; then
  echo "feedctl install: no checksum for ${binary} in SHA256SUMS" >&2
  exit 1
fi

if command -v sha256sum >/dev/null 2>&1; then
  if ! (cd "${work_dir}" && echo "${checksum_line}" | sha256sum -c - >/dev/null 2>&1); then
    echo "feedctl install: checksum verification failed for ${binary}" >&2
    exit 1
  fi
elif command -v shasum >/dev/null 2>&1; then
  if ! (cd "${work_dir}" && echo "${checksum_line}" | shasum -a 256 -c - >/dev/null 2>&1); then
    echo "feedctl install: checksum verification failed for ${binary}" >&2
    exit 1
  fi
else
  echo "feedctl install: neither sha256sum nor shasum is available to verify the download" >&2
  exit 1
fi

mkdir -p "${INSTALL_DIR}"
install_path="${INSTALL_DIR}/feedctl"
cp "${work_dir}/${binary}" "${install_path}"
chmod +x "${install_path}"

echo "feedctl install: installed to ${install_path}"
"${install_path}" --version
