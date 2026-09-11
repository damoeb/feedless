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

// confirmDelete prompts on stderr so it survives "> out.json"; without a TTY it refuses unless --yes.
// isTTY is a parameter so tests can cover every path without a real terminal.
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

// Only an *os.File can be a terminal.
func stdinIsTTY(r io.Reader) bool {
	f, ok := r.(*os.File)
	if !ok {
		return false
	}

	return output.IsTerminal(f)
}

// deleteDeclinedError is a deliberate no-op, not a failure: no "error:" prefix, exit 2.
type deleteDeclinedError struct{}

func (e *deleteDeclinedError) Error() string { return "confirmation declined" }

func (e *deleteDeclinedError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintln(w, "Cancelled.")
}

func (e *deleteDeclinedError) ExitCode() int { return ExitCancelled }
