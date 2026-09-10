package main

import (
	"bytes"
	"strings"
	"testing"

	"github.com/zalando/go-keyring"
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
// mapping end to end: internal/cmd's auth logout surfaces a
// *cmd.ExitError when there is nothing to log out of, and run() must
// translate its code (4, not the default 1) while still printing the
// error to stderr.
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
