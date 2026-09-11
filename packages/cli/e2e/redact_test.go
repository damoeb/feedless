//go:build e2e

package e2e

import (
	"regexp"
	"strings"
	"sync"
)

// CI keeps test output for a long time, so nothing the tests log or fail
// with may carry a live credential: every string that can contain output
// from feedctl, the core or a container goes through Redact first.
var (
	// jwtPattern matches anything JWT-shaped (base64url header starting
	// with {"…, payload, signature) — session tokens, API tokens.
	jwtPattern = regexp.MustCompile(`eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]*`)

	secretsMu sync.Mutex
	secrets   []string
)

// RegisterSecret makes Redact replace every occurrence of value from now on.
func RegisterSecret(value string) {
	if value == "" {
		return
	}

	secretsMu.Lock()
	defer secretsMu.Unlock()

	secrets = append(secrets, value)
}

// Redact replaces every registered secret and anything JWT-shaped in s.
func Redact(s string) string {
	secretsMu.Lock()
	known := append([]string(nil), secrets...)
	secretsMu.Unlock()

	for _, value := range known {
		s = strings.ReplaceAll(s, value, "[redacted]")
	}

	return jwtPattern.ReplaceAllString(s, "[redacted-jwt]")
}
