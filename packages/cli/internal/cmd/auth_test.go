package cmd

import (
	"bytes"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/zalando/go-keyring"

	"github.com/damoeb/feedless/packages/cli/internal/config"
)

// withTempConfigHome keeps tests off the real ~/.config/feedctl and OS keyring.
func withTempConfigHome(t *testing.T) {
	t.Helper()

	t.Setenv("XDG_CONFIG_HOME", t.TempDir())
	t.Setenv(config.EnvHost, "")
	t.Setenv(config.EnvToken, "")
	keyring.MockInit()
}

func userServer(t *testing.T, respond func(w http.ResponseWriter, r *http.Request)) *httptest.Server {
	t.Helper()

	srv := httptest.NewServer(http.HandlerFunc(respond))
	t.Cleanup(srv.Close)

	return srv
}

func okUserHandler(email string) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"id":"11111111-1111-1111-1111-111111111111","email":"` + email + `","groups":[]}`))
	}
}

func unauthorizedHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusUnauthorized)
	_, _ = w.Write([]byte(`{"message":"invalid token"}`))
}

// runCmd always builds feedctl as version "1.0.0".
func userServerWithVersion(t *testing.T, version, email string) *httptest.Server {
	t.Helper()

	return userServer(t, func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("X-Feedless-Version", version)
		okUserHandler(email)(w, r)
	})
}

func runCmd(args ...string) (stdout, stderr *bytes.Buffer, err error) {
	root := NewRootCmd("1.0.0")
	stdout, stderr = &bytes.Buffer{}, &bytes.Buffer{}
	root.SetOut(stdout)
	root.SetErr(stderr)
	root.SetArgs(args)

	err = root.Execute()

	return stdout, stderr, err
}

func runCmdWithStdin(stdin string, args ...string) (stdout, stderr *bytes.Buffer, err error) {
	root := NewRootCmd("1.0.0")
	stdout, stderr = &bytes.Buffer{}, &bytes.Buffer{}
	root.SetOut(stdout)
	root.SetErr(stderr)
	root.SetIn(strings.NewReader(stdin))
	root.SetArgs(args)

	err = root.Execute()

	return stdout, stderr, err
}

func TestAuthLogin_WithToken_Success(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, okUserHandler("someone@example.org"))

	stdout, stderr, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL, "--with-token")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	want := "Logged in to " + hostOf(srv.URL) + " as someone@example.org\n"
	if stdout.String() != want {
		t.Errorf("stdout = %q, want %q", stdout.String(), want)
	}

	cfg, loadErr := config.Load()
	if loadErr != nil {
		t.Fatalf("config.Load() error = %v", loadErr)
	}
	if cfg.DefaultHost != hostOf(srv.URL) {
		t.Errorf("DefaultHost = %q, want %q", cfg.DefaultHost, hostOf(srv.URL))
	}
	entry, ok := cfg.Hosts[hostOf(srv.URL)]
	if !ok {
		t.Fatalf("host entry missing, got %v", cfg.Hosts)
	}
	if entry.User != "someone@example.org" || entry.URL != srv.URL {
		t.Errorf("host entry = %+v, unexpected", entry)
	}
	if entry.Token != "" {
		t.Errorf("host entry Token = %q, want empty (keyring available)", entry.Token)
	}

	token, keyErr := keyring.Get(config.ServiceName, hostOf(srv.URL))
	if keyErr != nil || token != "the-token" {
		t.Errorf("keyring token = (%q, %v), want (the-token, nil)", token, keyErr)
	}
}

func TestAuthLogin_KeyringUnavailable_FallsBackToFileAndWarns(t *testing.T) {
	withTempConfigHome(t)
	// ErrUnsupportedPlatform is the "no keyring" case StoreToken falls back for.
	keyring.MockInitWithError(keyring.ErrUnsupportedPlatform)
	srv := userServer(t, okUserHandler("someone@example.org"))

	_, stderr, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL, "--with-token")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if !strings.Contains(stderr.String(), "plain text") {
		t.Errorf("stderr = %q, want a plain-text storage warning", stderr.String())
	}
	if strings.Contains(stderr.String(), "the-token") {
		t.Errorf("stderr = %q, must never contain the token", stderr.String())
	}

	cfg, loadErr := config.Load()
	if loadErr != nil {
		t.Fatalf("config.Load() error = %v", loadErr)
	}
	if cfg.Hosts[hostOf(srv.URL)].Token != "the-token" {
		t.Errorf("host entry Token = %q, want the-token stored as fallback", cfg.Hosts[hostOf(srv.URL)].Token)
	}
}

// A keyring error that isn't "no keyring" (locked, access denied) must not downgrade to plain text.
func TestAuthLogin_UnrecognizedKeyringError_FailsAndStoresNothing(t *testing.T) {
	withTempConfigHome(t)
	keyring.MockInitWithError(errors.New("keychain is locked"))
	srv := userServer(t, okUserHandler("someone@example.org"))

	_, stderr, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL, "--with-token")

	if err == nil {
		t.Fatal("Execute() error = nil, want an error for an unrecognized keyring failure")
	}
	if !strings.Contains(err.Error(), "keychain is locked") {
		t.Errorf("error = %q, want it to include the underlying keyring error", err.Error())
	}
	if strings.Contains(stderr.String(), "the-token") {
		t.Errorf("stderr = %q, must never contain the token", stderr.String())
	}

	cfg, loadErr := config.Load()
	if loadErr != nil {
		t.Fatalf("config.Load() error = %v", loadErr)
	}
	if len(cfg.Hosts) != 0 || cfg.DefaultHost != "" {
		t.Errorf("config = %+v, want nothing stored after a failed login", cfg)
	}
}

func TestAuthLogin_401_FailsWithExit4AndStoresNothing(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, unauthorizedHandler)

	_, stderr, err := runCmdWithStdin("bad-token\n", "auth", "login", "--url", srv.URL, "--with-token")

	assertExitCode(t, err, 4)
	if strings.Contains(stderr.String(), "bad-token") {
		t.Errorf("stderr = %q, must never contain the token", stderr.String())
	}

	cfg, loadErr := config.Load()
	if loadErr != nil {
		t.Fatalf("config.Load() error = %v", loadErr)
	}
	if len(cfg.Hosts) != 0 || cfg.DefaultHost != "" {
		t.Errorf("config = %+v, want nothing stored after a failed login", cfg)
	}
}

func TestAuthLogin_SecondHost_DoesNotChangeDefault(t *testing.T) {
	withTempConfigHome(t)
	first := userServer(t, okUserHandler("first@example.org"))
	second := userServer(t, okUserHandler("second@example.org"))

	if _, _, err := runCmdWithStdin("token-1\n", "auth", "login", "--url", first.URL, "--with-token"); err != nil {
		t.Fatalf("first login error = %v", err)
	}
	if _, _, err := runCmdWithStdin("token-2\n", "auth", "login", "--url", second.URL, "--with-token"); err != nil {
		t.Fatalf("second login error = %v", err)
	}

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}
	if cfg.DefaultHost != hostOf(first.URL) {
		t.Errorf("DefaultHost = %q, want it to stay %q", cfg.DefaultHost, hostOf(first.URL))
	}
	if len(cfg.Hosts) != 2 {
		t.Errorf("Hosts = %v, want 2 entries", cfg.Hosts)
	}
}

func TestAuthLogin_WithoutTokenFlag_NonTerminalStdin_Errors(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, okUserHandler("someone@example.org"))

	_, _, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL)

	if err == nil {
		t.Fatal("Execute() error = nil, want an error (stdin is not a terminal)")
	}
	if !strings.Contains(err.Error(), "--with-token") {
		t.Errorf("error = %q, want it to mention --with-token", err.Error())
	}
}

func TestAuthStatus_NoHosts_ReportsNotLoggedIn(t *testing.T) {
	withTempConfigHome(t)

	stdout, _, err := runCmd("auth", "status")
	if err != nil {
		t.Fatalf("Execute() error = %v", err)
	}
	if !strings.Contains(stdout.String(), "not logged in") {
		t.Errorf("stdout = %q, want it to say not logged in", stdout.String())
	}
}

func TestAuthStatus_ReportsOkAndDefaultMarker(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, okUserHandler("someone@example.org"))

	if _, _, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL, "--with-token"); err != nil {
		t.Fatalf("login error = %v", err)
	}

	stdout, _, err := runCmd("auth", "status")
	if err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	out := stdout.String()
	if !strings.Contains(out, hostOf(srv.URL)+" (default)") {
		t.Errorf("stdout = %q, want the default marker on %s", out, hostOf(srv.URL))
	}
	if !strings.Contains(out, "status=ok") {
		t.Errorf("stdout = %q, want status=ok", out)
	}
	if !strings.Contains(out, "token=keyring") {
		t.Errorf("stdout = %q, want token=keyring", out)
	}
}

func TestAuthStatus_FailedHost_ExitsWith1(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, unauthorizedHandler)

	cfg := &config.Config{
		DefaultHost: hostOf(srv.URL),
		Hosts:       map[string]config.HostEntry{hostOf(srv.URL): {URL: srv.URL, User: "someone@example.org"}},
	}
	if err := keyring.Set(config.ServiceName, hostOf(srv.URL), "stale-token"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	stdout, _, err := runCmd("auth", "status")

	assertExitCode(t, err, 1)
	if !strings.Contains(stdout.String(), "status=failed") {
		t.Errorf("stdout = %q, want status=failed", stdout.String())
	}
}

// auth status builds one Client per host; the shared Warner must still warn only once.
func TestAuthStatus_TwoMismatchedHosts_PrintsWarningOnce(t *testing.T) {
	withTempConfigHome(t)
	srv1 := userServerWithVersion(t, "9.9.1", "first@example.org")
	srv2 := userServerWithVersion(t, "9.9.2", "second@example.org")

	cfg := &config.Config{
		DefaultHost: hostOf(srv1.URL),
		Hosts: map[string]config.HostEntry{
			hostOf(srv1.URL): {URL: srv1.URL, User: "first@example.org"},
			hostOf(srv2.URL): {URL: srv2.URL, User: "second@example.org"},
		},
	}
	if err := keyring.Set(config.ServiceName, hostOf(srv1.URL), "token-1"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	if err := keyring.Set(config.ServiceName, hostOf(srv2.URL), "token-2"); err != nil {
		t.Fatalf("seeding keyring: %v", err)
	}
	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	stdout, stderr, err := runCmd("auth", "status")
	if err != nil {
		t.Fatalf("Execute() error = %v, stdout = %q", err, stdout.String())
	}

	if got := strings.Count(stderr.String(), "warning:"); got != 1 {
		t.Errorf("warning count = %d, want exactly 1 across both mismatched hosts; stderr = %q", got, stderr.String())
	}
}

func TestAuthLogout_RemovesHostAndKeyringToken(t *testing.T) {
	withTempConfigHome(t)
	srv := userServer(t, okUserHandler("someone@example.org"))

	if _, _, err := runCmdWithStdin("the-token\n", "auth", "login", "--url", srv.URL, "--with-token"); err != nil {
		t.Fatalf("login error = %v", err)
	}

	stdout, _, err := runCmd("auth", "logout")
	if err != nil {
		t.Fatalf("Execute() error = %v", err)
	}
	if !strings.Contains(stdout.String(), "Logged out of "+hostOf(srv.URL)) {
		t.Errorf("stdout = %q, want a logout confirmation", stdout.String())
	}

	cfg, loadErr := config.Load()
	if loadErr != nil {
		t.Fatalf("config.Load() error = %v", loadErr)
	}
	if len(cfg.Hosts) != 0 || cfg.DefaultHost != "" {
		t.Errorf("config = %+v, want the host entry and default_host removed", cfg)
	}
	if _, keyErr := keyring.Get(config.ServiceName, hostOf(srv.URL)); !errors.Is(keyErr, keyring.ErrNotFound) {
		t.Errorf("keyring.Get() error = %v, want ErrNotFound", keyErr)
	}
}

func TestAuthLogout_DefaultMovesToRemainingHost(t *testing.T) {
	withTempConfigHome(t)
	first := userServer(t, okUserHandler("first@example.org"))
	second := userServer(t, okUserHandler("second@example.org"))

	if _, _, err := runCmdWithStdin("token-1\n", "auth", "login", "--url", first.URL, "--with-token"); err != nil {
		t.Fatalf("first login error = %v", err)
	}
	if _, _, err := runCmdWithStdin("token-2\n", "auth", "login", "--url", second.URL, "--with-token"); err != nil {
		t.Fatalf("second login error = %v", err)
	}

	if _, _, err := runCmd("auth", "logout", "--host", hostOf(first.URL)); err != nil {
		t.Fatalf("logout error = %v", err)
	}

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}
	if cfg.DefaultHost != hostOf(second.URL) {
		t.Errorf("DefaultHost = %q, want it to move to %q", cfg.DefaultHost, hostOf(second.URL))
	}
	if _, ok := cfg.Hosts[hostOf(first.URL)]; ok {
		t.Errorf("Hosts still contains the logged-out host: %v", cfg.Hosts)
	}
}

func TestAuthLogout_NotLoggedIn_ExitsWith4(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("auth", "logout")

	assertExitCode(t, err, 4)
	if !strings.Contains(err.Error(), "not logged in") {
		t.Errorf("error = %q, want a not-logged-in message", err.Error())
	}
}

func hostOf(rawURL string) string {
	return strings.TrimPrefix(strings.TrimPrefix(rawURL, "https://"), "http://")
}

func assertExitCode(t *testing.T, err error, want int) {
	t.Helper()

	var ec exitCoder
	if !errors.As(err, &ec) {
		t.Fatalf("error = %v, want an error with ExitCode()", err)
	}
	if got := ec.ExitCode(); got != want {
		t.Errorf("ExitCode() = %d, want %d", got, want)
	}
}

// Mirrors main.go's unexported interface.
type exitCoder interface {
	ExitCode() int
}
