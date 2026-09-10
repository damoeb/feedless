package main

import (
	"bytes"
	"context"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/zalando/go-keyring"

	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/cmd"
	"github.com/damoeb/feedless/packages/cli/internal/config"
)

func TestRun_UnknownFlag_ReportsErrorToStderr(t *testing.T) {
	stdout := &bytes.Buffer{}
	stderr := &bytes.Buffer{}

	code := run([]string{"--bogus-flag"}, stdout, stderr)

	if code != 1 {
		t.Errorf("run() exit code = %d, want 1", code)
	}

	got := stderr.String()
	want := "error: unknown flag: --bogus-flag\n"
	if got != want {
		t.Errorf("stderr = %q, want %q", got, want)
	}

	if stdout.Len() != 0 {
		t.Errorf("stdout = %q, want empty", stdout.String())
	}
}

func TestRun_Version_Succeeds(t *testing.T) {
	stdout := &bytes.Buffer{}
	stderr := &bytes.Buffer{}

	code := run([]string{"--version"}, stdout, stderr)

	if code != 0 {
		t.Errorf("run() exit code = %d, want 0", code)
	}

	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want empty", stderr.String())
	}

	if !strings.HasPrefix(stdout.String(), "feedctl version ") {
		t.Errorf("stdout = %q, want it to start with %q", stdout.String(), "feedctl version ")
	}
}

// TestRun_NotLoggedIn_ExitsWith4AndPrintsError exercises run()'s exit-code
// mapping end to end through an actual command: internal/cmd's auth
// logout returns a *config.NotLoggedInError directly when there is
// nothing to log out of, and run() must recognize its ExitCode() method
// (4, not the default 1) while still printing the error to stderr.
func TestRun_NotLoggedIn_ExitsWith4AndPrintsError(t *testing.T) {
	t.Setenv("XDG_CONFIG_HOME", t.TempDir())
	t.Setenv("FEEDCTL_HOST", "")
	t.Setenv("FEEDCTL_TOKEN", "")
	keyring.MockInit()

	stdout := &bytes.Buffer{}
	stderr := &bytes.Buffer{}

	code := run([]string{"auth", "logout"}, stdout, stderr)

	if code != 4 {
		t.Errorf("run() exit code = %d, want 4", code)
	}
	if !strings.Contains(stderr.String(), "not logged in") {
		t.Errorf("stderr = %q, want a not-logged-in message", stderr.String())
	}
}

// TestExitCodeFor_NotLoggedInFromResolve_Is4 and
// TestExitCodeFor_NotLoggedInFromNewFromConfig_Is4 test exitCodeFor — the
// exact function run() uses to pick a process exit code — against real
// errors produced by config.Resolve and client.NewFromConfig, the shared
// resolution path every future C3-C5 command builds its authenticated
// client on. There is no such command yet to drive through run() as a full
// subprocess-style invocation (only auth's own subcommands exist, and they
// each have bespoke host/token handling per their own requirements), so
// this is the most direct way to prove that any future command relying on
// NewFromConfig — not just auth logout's hand-built error — exits 4, not
// the default 1, the moment it's wired up.
func TestExitCodeFor_NotLoggedInFromResolve_Is4(t *testing.T) {
	cfg := &config.Config{}

	_, err := config.Resolve(cfg, "")
	if err == nil {
		t.Fatal("config.Resolve() error = nil, want *config.NotLoggedInError")
	}

	if code := exitCodeFor(err); code != 4 {
		t.Errorf("exitCodeFor(config.Resolve() error) = %d, want 4", code)
	}
}

func TestExitCodeFor_NotLoggedInFromNewFromConfig_Is4(t *testing.T) {
	keyring.MockInit()
	cfg := &config.Config{}

	_, _, err := client.NewFromConfig(cfg, "", "1.0.0", &bytes.Buffer{})
	if err == nil {
		t.Fatal("client.NewFromConfig() error = nil, want *config.NotLoggedInError")
	}

	if code := exitCodeFor(err); code != 4 {
		t.Errorf("exitCodeFor(client.NewFromConfig() error) = %d, want 4", code)
	}
}

// TestRun_APIError_RendersMessageAndFieldErrors exercises run()'s
// extended error rendering end to end through `feedctl api`: a
// VALIDATION_ERROR response's message and field errors (cmd.APIError,
// C3's one error mapping) must reach stderr formatted exactly as
// requirement 3 specifies — "error: <message>" then one indented
// "field: message" line per field error — and the process must exit 1
// (not 4; this is a 400, not a 401).
func TestRun_APIError_RendersMessageAndFieldErrors(t *testing.T) {
	t.Setenv("XDG_CONFIG_HOME", t.TempDir())
	keyring.MockInit()

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		_, _ = w.Write([]byte(`{"code":"VALIDATION_ERROR","message":"invalid request","errors":[{"field":"url","message":"must not be blank"}]}`))
	}))
	t.Cleanup(srv.Close)

	host := strings.TrimPrefix(srv.URL, "http://")
	if _, _, err := config.StoreToken(host, "tok", io.Discard); err != nil {
		t.Fatalf("StoreToken() error = %v", err)
	}

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}

	cfg.Hosts[host] = config.HostEntry{URL: srv.URL, User: "tester"}
	cfg.DefaultHost = host

	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	stdout := &bytes.Buffer{}
	stderr := &bytes.Buffer{}

	code := run([]string{"api", "repositories"}, stdout, stderr)

	if code != 1 {
		t.Errorf("run() exit code = %d, want 1", code)
	}

	want := "error: invalid request\n  url: must not be blank\n"
	if got := stderr.String(); got != want {
		t.Errorf("stderr = %q, want %q", got, want)
	}
}

// TestRunWithContext_CancelledContext_ExitsWithCode2AndCancelledMessage
// drives run's SIGINT-cancellation mapping (requirement 3: exit code 2)
// without sending the process a real signal — runWithContext takes the
// context run() would otherwise build from signal.NotifyContext, so a test
// can hand it one that's already cancelled and observe the same mapping
// run() applies.
func TestRunWithContext_CancelledContext_ExitsWithCode2AndCancelledMessage(t *testing.T) {
	t.Setenv("XDG_CONFIG_HOME", t.TempDir())
	keyring.MockInit()

	host := "example.invalid"
	if _, _, err := config.StoreToken(host, "tok", io.Discard); err != nil {
		t.Fatalf("StoreToken() error = %v", err)
	}

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}

	cfg.Hosts[host] = config.HostEntry{URL: "http://" + host, User: "tester"}
	cfg.DefaultHost = host

	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	ctx, cancel := context.WithCancel(context.Background())
	cancel()

	stdout := &bytes.Buffer{}
	stderr := &bytes.Buffer{}

	// auth status builds a Client and calls the API with cmd.Context() —
	// already-cancelled, so the request fails immediately and the command
	// itself returns a non-nil error (a *cmd.ExitError with code 1); run's
	// cancellation check must override that to cmd.ExitCancelled.
	code := runWithContext(ctx, []string{"auth", "status"}, stdout, stderr)

	if code != cmd.ExitCancelled {
		t.Errorf("runWithContext() exit code = %d, want %d (cmd.ExitCancelled)", code, cmd.ExitCancelled)
	}
	if !strings.Contains(stderr.String(), "cancelled") {
		t.Errorf("stderr = %q, want it to mention cancellation", stderr.String())
	}
}
