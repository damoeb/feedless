package config

import (
	"fmt"
	"io"

	"github.com/zalando/go-keyring"
)

// ServiceName is the OS keyring service under which every host's token is
// stored; the account is the host name (the hosts.yml map key).
const ServiceName = "feedctl"

// TokenSourceKeyring, TokenSourceFile and TokenSourceEnv name where a
// resolved token came from, for display (feedctl auth status) and for
// deciding whether FEEDCTL_TOKEN applies.
const (
	TokenSourceEnv     = "FEEDCTL_TOKEN"
	TokenSourceKeyring = "keyring"
	TokenSourceFile    = "file"
)

// StoreToken stores token for host in the OS keyring. If no keyring exists
// at all on this machine (see isKeyringUnavailable), it falls back to
// returning the token so the caller can persist it in hosts.yml instead,
// and prints a warning to stderr that it is stored in plain text; any
// stale keyring entry for host is also deleted, so it can never outrank
// the fresh token now living in hosts.yml (see config.TokenForHost's
// keyring-before-file order).
//
// Any other keyring error (a locked keychain, access denied, a value too
// big for the backend, ...) is a real failure, not a "no keyring"
// situation: it is returned as-is, and the caller must store nothing.
//
// The returned source is TokenSourceKeyring or TokenSourceFile; fileToken
// is non-empty only for the file fallback and is what the caller should
// write into the host's HostEntry.Token.
func StoreToken(host, token string, stderr io.Writer) (source, fileToken string, err error) {
	setErr := keyring.Set(ServiceName, host, token)
	if setErr == nil {
		return TokenSourceKeyring, "", nil
	}

	if !isKeyringUnavailable(setErr) {
		return "", "", fmt.Errorf("storing the token for %s in the OS keyring: %w", host, setErr)
	}

	// A keyring that only just became unavailable (or never held anything
	// for this host) may still have a stale entry from an earlier login;
	// ignore any error here, matching RemoveToken.
	_ = keyring.Delete(ServiceName, host)

	path, pathErr := Path()
	if pathErr != nil {
		path = fileName
	}
	_, _ = fmt.Fprintf(stderr, "warning: no OS keyring available; storing the token for %s in plain text in %s\n", host, path)

	return TokenSourceFile, token, nil
}

// RemoveToken deletes host's token from the OS keyring, if present. A
// missing entry or an unavailable keyring is not an error: the caller is
// responsible for also removing any plain-text fallback from hosts.yml.
func RemoveToken(host string) {
	_ = keyring.Delete(ServiceName, host)
}

// keyringToken reads host's token from the OS keyring. A missing entry and
// an unavailable keyring both come back as ("", err) — callers fall
// through to the hosts.yml fallback for either.
func keyringToken(host string) (string, error) {
	token, err := keyring.Get(ServiceName, host)
	if err != nil {
		return "", err
	}

	return token, nil
}
