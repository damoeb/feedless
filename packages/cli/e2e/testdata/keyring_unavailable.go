package config

import "github.com/zalando/go-keyring"

// Not part of feedctl: the e2e harness overlays it so tokens land in hosts.yml, not the developer's keychain.
func init() {
	keyring.MockInitWithError(keyring.ErrUnsupportedPlatform)
}
