package cmd

import (
	"bytes"
	"context"
	"errors"
	"strings"
	"testing"

	"github.com/spf13/cobra"
)

// newConfirmTestCmd builds a bare *cobra.Command wired for direct,
// non-cobra-Execute testing of confirmDelete (and, in repository_test.go,
// deleteRepository) — same shape as source_editor_test.go's
// newEditorTestCmd. A context is set explicitly since cobra.Command.ctx is
// nil until SetContext/ExecuteContext runs it, and deleteRepository's own
// tests pass cmd.Context() straight into the generated API client.
func newConfirmTestCmd(stdin string) (*cobra.Command, *bytes.Buffer, *bytes.Buffer) {
	c := &cobra.Command{}
	c.SetContext(context.Background())

	var stdout, stderr bytes.Buffer
	c.SetOut(&stdout)
	c.SetErr(&stderr)
	c.SetIn(strings.NewReader(stdin))

	return c, &stdout, &stderr
}

func TestConfirmDelete_Yes_SkipsPromptEntirely_EvenNonTTY(t *testing.T) {
	cmd, _, stderr := newConfirmTestCmd("")

	if err := confirmDelete(cmd, false, true, "Delete thing?"); err != nil {
		t.Fatalf("confirmDelete() error = %v, want nil (--yes skips the prompt)", err)
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed when --yes is given", stderr.String())
	}
}

func TestConfirmDelete_Yes_SkipsPromptEntirely_TTY(t *testing.T) {
	cmd, _, stderr := newConfirmTestCmd("")

	if err := confirmDelete(cmd, true, true, "Delete thing?"); err != nil {
		t.Fatalf("confirmDelete() error = %v, want nil", err)
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed when --yes is given even on a TTY", stderr.String())
	}
}

func TestConfirmDelete_NotTTY_NoYes_RefusedWithExactMessage(t *testing.T) {
	cmd, _, _ := newConfirmTestCmd("")

	err := confirmDelete(cmd, false, false, "Delete thing?")
	if err == nil {
		t.Fatal("confirmDelete() error = nil, want a refusal")
	}
	if err.Error() != "--yes is required when not running interactively" {
		t.Errorf("error = %q, want the exact brief message", err.Error())
	}

	// A plain error (not an ExitError/exitCoder) falls back to feedctl's
	// default exit code 1, not ExitCancelled.
	var cancelled *deleteDeclinedError
	if errors.As(err, &cancelled) {
		t.Error("error is *deleteDeclinedError, want a plain refusal error (exit 1, not 2)")
	}
}

func TestConfirmDelete_TTY_Accepted_Lowercase_y(t *testing.T) {
	cmd, _, stderr := newConfirmTestCmd("y\n")

	if err := confirmDelete(cmd, true, false, "Delete thing?"); err != nil {
		t.Fatalf("confirmDelete() error = %v", err)
	}
	if !strings.Contains(stderr.String(), "Delete thing? [y/N] ") {
		t.Errorf("stderr = %q, want the prompt with the appended [y/N]", stderr.String())
	}
}

func TestConfirmDelete_TTY_Accepted_FullWordYesAndMixedCase(t *testing.T) {
	tests := []string{"yes", "Y", "YES", "  yes  \n"}

	for _, answer := range tests {
		t.Run(answer, func(t *testing.T) {
			cmd, _, _ := newConfirmTestCmd(answer)

			if err := confirmDelete(cmd, true, false, "Delete thing?"); err != nil {
				t.Fatalf("confirmDelete() error = %v, want acceptance of %q", err, answer)
			}
		})
	}
}

func TestConfirmDelete_TTY_Declined_ExitCode2_NoErrorPrefix(t *testing.T) {
	tests := []struct {
		name  string
		stdin string
	}{
		{"explicit n", "n\n"},
		{"empty answer (bare enter)", "\n"},
		{"no answer at all (EOF)", ""},
		{"garbage", "maybe\n"},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			cmd, _, _ := newConfirmTestCmd(tc.stdin)

			err := confirmDelete(cmd, true, false, "Delete thing?")

			var declined *deleteDeclinedError
			if !errors.As(err, &declined) {
				t.Fatalf("confirmDelete() error = %v, want *deleteDeclinedError", err)
			}
			if declined.ExitCode() != ExitCancelled {
				t.Errorf("ExitCode() = %d, want %d", declined.ExitCode(), ExitCancelled)
			}

			var rendered bytes.Buffer
			declined.RenderError(&rendered)
			if rendered.String() != "Cancelled.\n" {
				t.Errorf("RenderError() = %q, want the exact cancellation message with no \"error: \" prefix", rendered.String())
			}
		})
	}
}

func TestStdinIsTTY_NonOSFile_False(t *testing.T) {
	if stdinIsTTY(strings.NewReader("")) {
		t.Error("stdinIsTTY(non-*os.File) = true, want false")
	}
}
