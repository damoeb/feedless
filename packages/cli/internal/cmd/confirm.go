package cmd

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"os"
	"strings"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// confirmDelete is the one reusable "are you sure?" flow every entity's
// delete command shares — repo delete here, C7's batch record delete and
// C8's source delete both reuse it as-is. yes is the caller's --yes flag:
// when true, the prompt is skipped entirely and confirmDelete returns nil
// immediately (no TTY check, no read — --yes behaves identically whether
// or not stdin happens to be a terminal). prompt is the full question
// without "[y/N]" (confirmDelete appends that itself), e.g.
// `Delete repository "title" (id)?` or, for a batch,
// `Delete 3 records from <repo>?`.
//
// When yes is false, isTTY decides between:
//   - not a TTY: refused with the exact message the brief specifies,
//     "--yes is required when not running interactively" — a plain error,
//     so main falls back to the default exit code 1.
//   - a TTY: the prompt is written to cmd.ErrOrStderr() (so it survives
//     `feedctl ... > out.json`, unlike stdout), one line is read from
//     cmd.InOrStdin(), and a case-insensitive "y"/"yes" answer confirms
//     (nil returned); anything else — including no answer at all (EOF) —
//     declines, returning *deleteDeclinedError (exit 2).
//
// isTTY is a parameter, not detected inside confirmDelete, precisely so
// it's injectable in tests without a real terminal: production callers
// pass stdinIsTTY(cmd.InOrStdin()); tests pass a literal bool alongside a
// fake (non-*os.File) reader set via cmd.SetIn, so both the
// "accepted"/"declined" TTY paths and the non-TTY refusal are exercised
// without ever needing a real TTY — see confirm_test.go.
func confirmDelete(cmd *cobra.Command, isTTY, yes bool, prompt string) error {
	if yes {
		return nil
	}

	if !isTTY {
		return errors.New("--yes is required when not running interactively")
	}

	_, _ = fmt.Fprintf(cmd.ErrOrStderr(), "%s [y/N] ", prompt)

	reader := bufio.NewReader(cmd.InOrStdin())
	line, _ := reader.ReadString('\n')
	answer := strings.ToLower(strings.TrimSpace(line))

	if answer == "y" || answer == "yes" {
		return nil
	}

	return &deleteDeclinedError{}
}

// stdinIsTTY reports whether r is an interactive terminal — only an
// *os.File can be one (mirrors poller.go's progressIsTTY for the output
// side, and auth.go's readToken for stdin specifically). Production delete
// commands pass stdinIsTTY(cmd.InOrStdin()) as confirmDelete's isTTY.
func stdinIsTTY(r io.Reader) bool {
	f, ok := r.(*os.File)
	if !ok {
		return false
	}

	return output.IsTerminal(f)
}

// deleteDeclinedError is returned by confirmDelete when the user declines
// the confirmation prompt. Like editCancelledError, this is a deliberate
// no-op, not a failure: it renders without the "error: " prefix and exits 2
// (cmd.ExitCancelled) — the same code source update --editor's cancellation
// uses.
type deleteDeclinedError struct{}

func (e *deleteDeclinedError) Error() string { return "confirmation declined" }

func (e *deleteDeclinedError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintln(w, "Cancelled.")
}

func (e *deleteDeclinedError) ExitCode() int { return ExitCancelled }
