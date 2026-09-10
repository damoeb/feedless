package main

import (
	"bytes"
	"strings"
	"testing"

	"github.com/zalando/go-keyring"

	"github.com/damoeb/feedless/packages/cli/internal/client"
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
