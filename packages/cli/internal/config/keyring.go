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

// StoreToken stores token for host in the OS keyring. If no keyring is
// available (no Secret Service, CI, ...), it falls back to returning the
// token so the caller can persist it in hosts.yml instead, and prints a
// warning to stderr that it is stored in plain text.
//
// The returned source is TokenSourceKeyring or TokenSourceFile; fileToken
// is non-empty only for the file fallback and is what the caller should
// write into the host's HostEntry.Token.
func StoreToken(host, token string, stderr io.Writer) (source, fileToken string) {
	if err := keyring.Set(ServiceName, host, token); err == nil {
		return TokenSourceKeyring, ""
	}

	path, pathErr := Path()
	if pathErr != nil {
		path = fileName
	}
	_, _ = fmt.Fprintf(stderr, "warning: no OS keyring available; storing the token for %s in plain text in %s\n", host, path)

	return TokenSourceFile, token
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
