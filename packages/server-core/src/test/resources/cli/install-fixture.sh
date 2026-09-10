#!/bin/sh
# Test fixture for CliInstallScriptControllerIntTest -- a stand-in for the
# real packages/cli/install.sh, small enough to assert placeholder
# substitution against without depending on the real script's content.
FEEDCTL_BASE_URL="${FEEDCTL_BASE_URL:-__FEEDCTL_BASE_URL__}"
echo "$FEEDCTL_BASE_URL"
