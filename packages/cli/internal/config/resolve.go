package config

import (
	"fmt"
	"os"
)

// EnvHost and EnvToken are the environment variables that participate in
// host/token resolution (see Resolve).
const (
	EnvHost  = "FEEDCTL_HOST"
	EnvToken = "FEEDCTL_TOKEN"
)

// NotLoggedInError means feedctl could not resolve a host and a token to
// use for a request. Host is empty when no host could be resolved at all
// (no --host/FEEDCTL_HOST/default_host); otherwise it names the host that
// has no usable token.
type NotLoggedInError struct {
	Host string
}

func (e *NotLoggedInError) Error() string {
	if e.Host == "" {
		return "not logged in — run: feedctl auth login --url <url>"
	}

	return fmt.Sprintf("not logged in to %s — run: feedctl auth login --url <url>", e.Host)
}

// Resolved is a host and token pair ready to build an authenticated
// client with (see client.New / client.NewFromConfig).
type Resolved struct {
	Host        string
	URL         string
	Token       string
	TokenSource string
}

// ResolveHost picks which host a command should act on: the --host flag
// value (if non-empty), else FEEDCTL_HOST, else the configured default
// host. It never errors — an empty result means no host could be
// resolved.
func ResolveHost(cfg *Config, flagHost string) string {
	if flagHost != "" {
		return flagHost
	}
	if h := os.Getenv(EnvHost); h != "" {
		return h
	}

	return cfg.DefaultHost
}

// TokenForHost resolves the token to use for host: FEEDCTL_TOKEN (only
// when applyEnv is true — callers pass true for the single host a command
// is actually targeting, false when merely listing other configured hosts,
// since the env var isn't itself host-scoped), else the OS keyring, else
// the plain-text fallback in hosts.yml.
func TokenForHost(cfg *Config, host string, applyEnv bool) (token, source string) {
	if applyEnv {
		if t := os.Getenv(EnvToken); t != "" {
			return t, TokenSourceEnv
		}
	}

	if t, err := keyringToken(host); err == nil && t != "" {
		return t, TokenSourceKeyring
	}

	if entry, ok := cfg.Hosts[host]; ok && entry.Token != "" {
		return entry.Token, TokenSourceFile
	}

	return "", ""
}

// Resolve follows feedctl's standard resolution order for the host a
// command should act on and the token to authenticate with:
//
//	host:  --host flag > FEEDCTL_HOST > default_host
//	token: FEEDCTL_TOKEN > keyring > hosts.yml
//
// It fails with *NotLoggedInError when no host can be resolved, when the
// resolved host has no usable token, or when the resolved host has a
// token but no configured URL to reach it on.
func Resolve(cfg *Config, flagHost string) (*Resolved, error) {
	host := ResolveHost(cfg, flagHost)
	if host == "" {
		return nil, &NotLoggedInError{}
	}

	token, source := TokenForHost(cfg, host, true)
	if token == "" {
		return nil, &NotLoggedInError{Host: host}
	}

	entry, ok := cfg.Hosts[host]
	if !ok || entry.URL == "" {
		return nil, &NotLoggedInError{Host: host}
	}

	return &Resolved{Host: host, URL: entry.URL, Token: token, TokenSource: source}, nil
}
