package cmd

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"os"
	"strings"
	"testing"

	"github.com/spf13/cobra"
)

// These cover runEditorLoop itself; the per-resource editor tests cover the wiring.

func newLoopTestCmd() (*cobra.Command, *bytes.Buffer, *bytes.Buffer) {
	c := &cobra.Command{}
	c.SetContext(context.Background())

	var stdout, stderr bytes.Buffer
	c.SetOut(&stdout)
	c.SetErr(&stderr)

	return c, &stdout, &stderr
}

func jsonSyntaxValidate(s string) error {
	var v any

	return json.Unmarshal([]byte(s), &v)
}

func tempFileGone(t *testing.T, path string) bool {
	t.Helper()

	_, err := os.Stat(path)
	if err == nil {
		return false
	}
	if errors.Is(err, os.ErrNotExist) {
		return true
	}

	t.Fatalf("os.Stat(%s) error = %v", path, err)

	return false
}

func findLoopTempFilePath(t *testing.T, stderr string) string {
	t.Helper()

	for _, line := range strings.Split(stderr, "\n") {
		if strings.Contains(line, "feedctl-loop-test-") {
			return strings.TrimSpace(line)
		}
	}

	t.Fatalf("stderr = %q, want a line with the kept temp file's path", stderr)

	return ""
}

func TestRunEditorLoop_Success_DeletesTempFileAndPrintsNothingOnStderr(t *testing.T) {
	cmd, _, stderr := newLoopTestCmd()

	cfg := editorLoopConfig{
		tempFilePattern: "feedctl-loop-test-*.json",
		nouns:           editorNouns{Subject: "the test document", Noun: "document"},
		get:             func(_ context.Context) (string, error) { return `{"n":1}`, nil },
		validate:        jsonSyntaxValidate,
		attempt: func(_ context.Context, _ string) (editorAttemptResult, error) {
			return editorAttemptResult{Done: true}, nil
		},
	}

	var tmpPathSeen string
	editor := func(path string) error {
		tmpPathSeen = path

		return os.WriteFile(path, []byte(`{"n":2}`), 0o600)
	}

	err := runEditorLoop(cmd, cfg, editor)
	if err != nil {
		t.Fatalf("runEditorLoop() error = %v, stderr = %q", err, stderr.String())
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed on success", stderr.String())
	}
	if !tempFileGone(t, tmpPathSeen) {
		t.Errorf("temp file %s still exists after success, want it deleted", tmpPathSeen)
	}
}

func TestRunEditorLoop_UnchangedOrEmpty_CancelledExit2_KeepsTempFile(t *testing.T) {
	tests := []struct {
		name   string
		editor editorFunc
	}{
		{"unchanged", func(_ string) error { return nil }},
		{"empty", func(path string) error { return os.WriteFile(path, []byte(""), 0o600) }},
		{"blank", func(path string) error { return os.WriteFile(path, []byte("  \n \n"), 0o600) }},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			cmd, _, stderr := newLoopTestCmd()

			cfg := editorLoopConfig{
				tempFilePattern: "feedctl-loop-test-*.json",
				nouns:           editorNouns{Subject: "the test document", Noun: "document"},
				get:             func(_ context.Context) (string, error) { return `{"n":1}`, nil },
				validate:        jsonSyntaxValidate,
				attempt: func(_ context.Context, _ string) (editorAttemptResult, error) {
					t.Fatal("attempt called, want the cancel check to short-circuit before any attempt")

					return editorAttemptResult{}, nil
				},
			}

			err := runEditorLoop(cmd, cfg, tc.editor)

			var cancelled *editCancelledError
			if !errors.As(err, &cancelled) {
				t.Fatalf("runEditorLoop() error = %v, want *editCancelledError", err)
			}
			if cancelled.ExitCode() != ExitCancelled {
				t.Errorf("ExitCode() = %d, want %d", cancelled.ExitCode(), ExitCancelled)
			}

			path := findLoopTempFilePath(t, stderr.String())
			if tempFileGone(t, path) {
				t.Errorf("temp file %s missing, want it kept on cancellation", path)
			}
		})
	}
}

func TestRunEditorLoop_InvalidJSON_ReopenedWithComment_ThenCancelsIfUnchanged(t *testing.T) {
	cmd, _, stderr := newLoopTestCmd()

	cfg := editorLoopConfig{
		tempFilePattern: "feedctl-loop-test-*.json",
		nouns:           editorNouns{Subject: "the test document", Noun: "document"},
		get:             func(_ context.Context) (string, error) { return `{"n":1}`, nil },
		validate:        jsonSyntaxValidate,
		attempt: func(_ context.Context, _ string) (editorAttemptResult, error) {
			t.Fatal("attempt called, want invalid JSON caught locally first")

			return editorAttemptResult{}, nil
		},
	}

	calls := 0
	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte("{ not valid"), 0o600)
		}

		// Second call: leave the reopened file untouched; this must cancel.
		return nil
	}

	err := runEditorLoop(cmd, cfg, editor)

	var cancelled *editCancelledError
	if !errors.As(err, &cancelled) {
		t.Fatalf("runEditorLoop() error = %v, want *editCancelledError", err)
	}
	if calls != 2 {
		t.Errorf("editor called %d times, want exactly 2 (one reopen, no third)", calls)
	}

	path := findLoopTempFilePath(t, stderr.String())

	raw, readErr := os.ReadFile(path)
	if readErr != nil {
		t.Fatalf("reading kept temp file: %v", readErr)
	}
	if !strings.Contains(string(raw), "Could not parse the edited document as JSON") {
		t.Errorf("temp file = %q, want the invalid-JSON comment naming the configured noun", raw)
	}
	if !strings.Contains(string(raw), "{ not valid") {
		t.Errorf("temp file = %q, want the user's invalid text preserved intact", raw)
	}
}

func TestRunEditorLoop_AttemptReopen_RewritesFileAndReopensEditor(t *testing.T) {
	cmd, _, _ := newLoopTestCmd()

	attempts := 0

	cfg := editorLoopConfig{
		tempFilePattern: "feedctl-loop-test-*.json",
		nouns:           editorNouns{Subject: "the test document", Noun: "document"},
		get:             func(_ context.Context) (string, error) { return `{"n":1}`, nil },
		validate:        jsonSyntaxValidate,
		attempt: func(_ context.Context, editedBody string) (editorAttemptResult, error) {
			attempts++
			if attempts == 1 {
				return editorAttemptResult{Comment: "# rejected once\n", Body: `{"n":3}`}, nil
			}

			return editorAttemptResult{Done: true}, nil
		},
	}

	calls := 0
	sawComment := false
	sawRewrittenBody := false

	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte(`{"n":2}`), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "rejected once") {
			sawComment = true
		}
		if strings.Contains(content, `{"n":3}`) {
			sawRewrittenBody = true
		}

		// A further change, so this isn't a no-op against the new baseline.
		return os.WriteFile(path, []byte(`{"n":4}`), 0o600)
	}

	err := runEditorLoop(cmd, cfg, editor)
	if err != nil {
		t.Fatalf("runEditorLoop() error = %v", err)
	}
	if attempts != 2 {
		t.Errorf("attempt called %d times, want 2 (rejected once, then succeeded)", attempts)
	}
	if calls != 2 {
		t.Errorf("editor called %d times, want 2 (reopened once)", calls)
	}
	if !sawComment {
		t.Error("reopened file did not contain attempt's reopen comment")
	}
	if !sawRewrittenBody {
		t.Error("reopened file's editable content was not attempt's rewritten Body")
	}
}

func TestRunEditorLoop_AttemptError_Terminal_KeepsTempFile(t *testing.T) {
	cmd, _, stderr := newLoopTestCmd()

	boom := errors.New("boom")

	cfg := editorLoopConfig{
		tempFilePattern: "feedctl-loop-test-*.json",
		nouns:           editorNouns{Subject: "the test document", Noun: "document"},
		get:             func(_ context.Context) (string, error) { return `{"n":1}`, nil },
		validate:        jsonSyntaxValidate,
		attempt: func(_ context.Context, _ string) (editorAttemptResult, error) {
			return editorAttemptResult{}, boom
		},
	}

	err := runEditorLoop(cmd, cfg, func(path string) error {
		return os.WriteFile(path, []byte(`{"n":2}`), 0o600)
	})
	if !errors.Is(err, boom) {
		t.Fatalf("runEditorLoop() error = %v, want it to wrap %v", err, boom)
	}

	path := findLoopTempFilePath(t, stderr.String())
	if tempFileGone(t, path) {
		t.Errorf("temp file %s missing, want it kept after a terminal attempt error", path)
	}
}

func TestRunEditorLoop_GetFails_NoTempFileCreated_EditorNeverCalled(t *testing.T) {
	cmd, _, stderr := newLoopTestCmd()

	boom := errors.New("get failed")

	cfg := editorLoopConfig{
		tempFilePattern: "feedctl-loop-test-*.json",
		nouns:           editorNouns{Subject: "the test document", Noun: "document"},
		get:             func(_ context.Context) (string, error) { return "", boom },
		validate:        jsonSyntaxValidate,
		attempt: func(_ context.Context, _ string) (editorAttemptResult, error) {
			t.Fatal("attempt called despite get failing")

			return editorAttemptResult{}, nil
		},
	}

	editorCalled := false

	err := runEditorLoop(cmd, cfg, func(_ string) error {
		editorCalled = true

		return nil
	})
	if !errors.Is(err, boom) {
		t.Fatalf("runEditorLoop() error = %v, want it to wrap %v", err, boom)
	}
	if editorCalled {
		t.Error("editor was launched despite the initial get failing")
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed (no temp file was ever created)", stderr.String())
	}
}
