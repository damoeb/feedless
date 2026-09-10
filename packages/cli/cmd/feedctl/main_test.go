package main

import (
	"bytes"
	"strings"
	"testing"
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
