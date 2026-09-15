package config

import (
	"fmt"
	"os"
)

const (
	EnvHost  = "FEEDCTL_HOST"
	EnvToken = "FEEDCTL_TOKEN"
)

// NotLoggedInError has an empty Host when no host resolved at all.
type NotLoggedInError struct {
	Host string
}

func (e *NotLoggedInError) Error() string {
	if e.Host == "" {
		return "not logged in — run: feedctl auth login --url <url>"
	}

	return fmt.Sprintf("not logged in to %s — run: feedctl auth login --url <url>", e.Host)
}

// ExitCode is 4, like every other auth failure.
func (e *NotLoggedInError) ExitCode() int { return 4 }

type Resolved struct {
	Host        string
	URL         string
	Token       string
	TokenSource string
}

// ResolveHost returns "" when no host resolves.
func ResolveHost(cfg *Config, flagHost string) string {
	if flagHost != "" {
		return flagHost
	}
	if h := os.Getenv(EnvHost); h != "" {
		return h
	}

	return cfg.DefaultHost
}

// FEEDCTL_TOKEN isn't host-scoped, so applyEnv is true only for the targeted host.
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

// Resolve order. Host: --host > FEEDCTL_HOST > default_host. Token: FEEDCTL_TOKEN > keyring > hosts.yml.
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
