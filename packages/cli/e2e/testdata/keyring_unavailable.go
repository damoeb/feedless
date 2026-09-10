package config

import "github.com/zalando/go-keyring"

// This file is not part of feedctl. The e2e harness (packages/cli/e2e)
// injects it into internal/config with `go build -overlay` when it builds
// the feedctl binary under test, so every keyring call fails with
// ErrUnsupportedPlatform: feedctl then takes its documented "no OS keyring"
// path and stores the token in hosts.yml under the test's XDG_CONFIG_HOME,
// instead of writing to the developer's real keychain. Everything else in
// the binary is the real feedctl.
func init() {
	keyring.MockInitWithError(keyring.ErrUnsupportedPlatform)
}
