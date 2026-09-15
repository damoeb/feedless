package config

import (
	"fmt"
	"io"

	"github.com/zalando/go-keyring"
)

// The account is the host name (the hosts.yml key).
const ServiceName = "feedctl"

const (
	TokenSourceEnv     = "FEEDCTL_TOKEN"
	TokenSourceKeyring = "keyring"
	TokenSourceFile    = "file"
)

// StoreToken falls back to hosts.yml only when no keyring exists; any other keyring error is returned and nothing is stored.
func StoreToken(host, token string, stderr io.Writer) (source, fileToken string, err error) {
	setErr := keyring.Set(ServiceName, host, token)
	if setErr == nil {
		return TokenSourceKeyring, "", nil
	}

	if !isKeyringUnavailable(setErr) {
		return "", "", fmt.Errorf("storing the token for %s in the OS keyring: %w", host, setErr)
	}

	// A stale keyring entry would outrank the new file token. Errors are ignored, like in RemoveToken.
	_ = keyring.Delete(ServiceName, host)

	path, pathErr := Path()
	if pathErr != nil {
		path = fileName
	}
	_, _ = fmt.Fprintf(stderr, "warning: no OS keyring available; storing the token for %s in plain text in %s\n", host, path)

	return TokenSourceFile, token, nil
}

// The caller also removes any plain-text fallback from hosts.yml.
func RemoveToken(host string) {
	_ = keyring.Delete(ServiceName, host)
}

// Missing and unavailable both return an error; callers fall through to hosts.yml.
func keyringToken(host string) (string, error) {
	token, err := keyring.Get(ServiceName, host)
	if err != nil {
		return "", err
	}

	return token, nil
}
