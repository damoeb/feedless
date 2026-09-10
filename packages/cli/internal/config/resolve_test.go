package config

import (
	"bytes"
	"errors"
	"strings"
	"testing"

	"github.com/zalando/go-keyring"
)

func TestResolveHost_FlagBeatsEnvBeatsDefault(t *testing.T) {
	cfg := &Config{DefaultHost: "default.example.org"}
	t.Setenv(EnvHost, "env.example.org")

	if got := ResolveHost(cfg, "flag.example.org"); got != "flag.example.org" {
		t.Errorf("ResolveHost() = %q, want flag value", got)
	}
}

func TestResolveHost_EnvBeatsDefault(t *testing.T) {
	cfg := &Config{DefaultHost: "default.example.org"}
	t.Setenv(EnvHost, "env.example.org")

	if got := ResolveHost(cfg, ""); got != "env.example.org" {
		t.Errorf("ResolveHost() = %q, want env value", got)
	}
}

func TestResolveHost_FallsBackToDefault(t *testing.T) {
	cfg := &Config{DefaultHost: "default.example.org"}
	t.Setenv(EnvHost, "")

	if got := ResolveHost(cfg, ""); got != "default.example.org" {
		t.Errorf("ResolveHost() = %q, want default_host", got)
	}
}

func TestResolveHost_NoneConfigured_ReturnsEmpty(t *testing.T) {
	cfg := &Config{}
	t.Setenv(EnvHost, "")

	if got := ResolveHost(cfg, ""); got != "" {
		t.Errorf("ResolveHost() = %q, want empty", got)
	}
}

func TestTokenForHost_EnvBeatsKeyringBeatsFile(t *testing.T) {
	keyring.MockInit()
	if err := keyring.Set(ServiceName, "h.example.org", "keyring-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	cfg := &Config{Hosts: map[string]HostEntry{"h.example.org": {Token: "file-token"}}}
	t.Setenv(EnvToken, "env-token")

	token, source := TokenForHost(cfg, "h.example.org", true)
	if token != "env-token" || source != TokenSourceEnv {
		t.Errorf("TokenForHost() = (%q, %q), want (env-token, %s)", token, source, TokenSourceEnv)
	}
}

func TestTokenForHost_EnvIgnoredWhenNotApplied(t *testing.T) {
	keyring.MockInit()
	if err := keyring.Set(ServiceName, "h.example.org", "keyring-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	cfg := &Config{}
	t.Setenv(EnvToken, "env-token")

	token, source := TokenForHost(cfg, "h.example.org", false)
	if token != "keyring-token" || source != TokenSourceKeyring {
		t.Errorf("TokenForHost() = (%q, %q), want (keyring-token, %s)", token, source, TokenSourceKeyring)
	}
}

func TestTokenForHost_KeyringBeatsFile(t *testing.T) {
	keyring.MockInit()
	if err := keyring.Set(ServiceName, "h.example.org", "keyring-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	cfg := &Config{Hosts: map[string]HostEntry{"h.example.org": {Token: "file-token"}}}
	t.Setenv(EnvToken, "")

	token, source := TokenForHost(cfg, "h.example.org", true)
	if token != "keyring-token" || source != TokenSourceKeyring {
		t.Errorf("TokenForHost() = (%q, %q), want (keyring-token, %s)", token, source, TokenSourceKeyring)
	}
}

func TestTokenForHost_FallsBackToFileWhenKeyringUnavailable(t *testing.T) {
	keyring.MockInitWithError(errors.New("no secret service"))
	cfg := &Config{Hosts: map[string]HostEntry{"h.example.org": {Token: "file-token"}}}
	t.Setenv(EnvToken, "")

	token, source := TokenForHost(cfg, "h.example.org", true)
	if token != "file-token" || source != TokenSourceFile {
		t.Errorf("TokenForHost() = (%q, %q), want (file-token, %s)", token, source, TokenSourceFile)
	}
}

func TestTokenForHost_NoneConfigured_ReturnsEmpty(t *testing.T) {
	keyring.MockInit()
	cfg := &Config{}
	t.Setenv(EnvToken, "")

	token, source := TokenForHost(cfg, "h.example.org", true)
	if token != "" || source != "" {
		t.Errorf("TokenForHost() = (%q, %q), want (\"\", \"\")", token, source)
	}
}

func TestResolve_Success(t *testing.T) {
	keyring.MockInit()
	if err := keyring.Set(ServiceName, "h.example.org", "the-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	cfg := &Config{
		DefaultHost: "h.example.org",
		Hosts:       map[string]HostEntry{"h.example.org": {URL: "https://h.example.org"}},
	}
	t.Setenv(EnvHost, "")
	t.Setenv(EnvToken, "")

	resolved, err := Resolve(cfg, "")
	if err != nil {
		t.Fatalf("Resolve() error = %v", err)
	}
	if resolved.Host != "h.example.org" || resolved.URL != "https://h.example.org" || resolved.Token != "the-token" || resolved.TokenSource != TokenSourceKeyring {
		t.Errorf("Resolve() = %+v, unexpected", resolved)
	}
}

func TestResolve_NoHost_ReturnsNotLoggedIn(t *testing.T) {
	keyring.MockInit()
	cfg := &Config{}
	t.Setenv(EnvHost, "")

	_, err := Resolve(cfg, "")

	var nle *NotLoggedInError
	if !errors.As(err, &nle) {
		t.Fatalf("Resolve() error = %v, want *NotLoggedInError", err)
	}
	if nle.Host != "" {
		t.Errorf("NotLoggedInError.Host = %q, want empty", nle.Host)
	}
	want := "not logged in — run: feedctl auth login --url <url>"
	if nle.Error() != want {
		t.Errorf("Error() = %q, want %q", nle.Error(), want)
	}
}

func TestResolve_NoToken_ReturnsNotLoggedInWithHost(t *testing.T) {
	keyring.MockInit()
	cfg := &Config{
		DefaultHost: "h.example.org",
		Hosts:       map[string]HostEntry{"h.example.org": {URL: "https://h.example.org"}},
	}
	t.Setenv(EnvHost, "")
	t.Setenv(EnvToken, "")

	_, err := Resolve(cfg, "")

	var nle *NotLoggedInError
	if !errors.As(err, &nle) {
		t.Fatalf("Resolve() error = %v, want *NotLoggedInError", err)
	}
	want := "not logged in to h.example.org — run: feedctl auth login --url <url>"
	if nle.Error() != want {
		t.Errorf("Error() = %q, want %q", nle.Error(), want)
	}
}

func TestStoreToken_KeyringAvailable_DoesNotFallBackToFile(t *testing.T) {
	keyring.MockInit()
	stderr := &bytes.Buffer{}

	source, fileToken := StoreToken("h.example.org", "secret-token", stderr)

	if source != TokenSourceKeyring {
		t.Errorf("source = %q, want %s", source, TokenSourceKeyring)
	}
	if fileToken != "" {
		t.Errorf("fileToken = %q, want empty when keyring succeeds", fileToken)
	}
	if stderr.String() != "" {
		t.Errorf("stderr = %q, want empty when keyring succeeds", stderr.String())
	}

	got, err := keyring.Get(ServiceName, "h.example.org")
	if err != nil || got != "secret-token" {
		t.Errorf("keyring.Get() = (%q, %v), want (secret-token, nil)", got, err)
	}
}

func TestStoreToken_KeyringUnavailable_FallsBackToFileAndWarns(t *testing.T) {
	keyring.MockInitWithError(errors.New("no secret service"))
	stderr := &bytes.Buffer{}

	source, fileToken := StoreToken("h.example.org", "secret-token", stderr)

	if source != TokenSourceFile {
		t.Errorf("source = %q, want %s", source, TokenSourceFile)
	}
	if fileToken != "secret-token" {
		t.Errorf("fileToken = %q, want secret-token", fileToken)
	}
	if !containsSecretTokenWarning(stderr.String()) {
		t.Errorf("stderr = %q, want a plain-text storage warning", stderr.String())
	}
	if containsToken(stderr.String(), "secret-token") {
		t.Errorf("stderr = %q, must never contain the token itself", stderr.String())
	}
}

func TestRemoveToken_DeletesFromKeyring(t *testing.T) {
	keyring.MockInit()
	if err := keyring.Set(ServiceName, "h.example.org", "secret-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}

	RemoveToken("h.example.org")

	if _, err := keyring.Get(ServiceName, "h.example.org"); !errors.Is(err, keyring.ErrNotFound) {
		t.Errorf("keyring.Get() error = %v, want ErrNotFound", err)
	}
}

func TestRemoveToken_KeyringUnavailable_DoesNotPanic(t *testing.T) {
	keyring.MockInitWithError(errors.New("no secret service"))

	RemoveToken("h.example.org")
}

func containsSecretTokenWarning(s string) bool {
	return strings.Contains(s, "plain text") || strings.Contains(s, "warning")
}

func containsToken(s, token string) bool {
	return strings.Contains(s, token)
}
