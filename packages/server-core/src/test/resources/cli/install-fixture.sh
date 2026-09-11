#!/bin/sh
# Stand-in for packages/cli/install.sh, so the test doesn't depend on the real script.
FEEDCTL_BASE_URL="${FEEDCTL_BASE_URL:-__FEEDCTL_BASE_URL__}"
echo "$FEEDCTL_BASE_URL"
