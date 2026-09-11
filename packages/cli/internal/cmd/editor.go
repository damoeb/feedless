package cmd

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"os/exec"
	"strings"

	"github.com/spf13/cobra"
)

// editorFunc is the seam tests replace with a fake editor.
type editorFunc func(path string) error

// launchSystemEditor goes through "sh -c" so an editor with arguments (e.g. "code --wait") works, like git.
func launchSystemEditor(path string) error {
	editor := os.Getenv("VISUAL")
	if editor == "" {
		editor = os.Getenv("EDITOR")
	}
	if editor == "" {
		editor = "vi"
	}

	//nolint:gosec // editor comes from the user's own environment, not request input
	c := exec.Command("sh", "-c", editor+` "$1"`, "--", path)
	c.Stdin = os.Stdin
	c.Stdout = os.Stdout
	c.Stderr = os.Stderr

	if err := c.Run(); err != nil {
		return fmt.Errorf("running editor %q: %w", editor, err)
	}

	return nil
}

// editCancelledError prints a plain message instead of "error:" and exits 2: cancelling is not a failure.
type editCancelledError struct{}

func (e *editCancelledError) Error() string { return "edit cancelled, no changes made" }

func (e *editCancelledError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintln(w, "Edit cancelled, no changes made.")
}

func (e *editCancelledError) ExitCode() int { return ExitCancelled }

// Subject is ID-qualified ("repository <id>") for the header; Noun is used everywhere else.
type editorNouns struct {
	Subject string
	Noun    string
}

// A non-nil error from attempt is terminal; Done false reopens the editor with Comment and Body.
type editorAttemptResult struct {
	Done    bool
	Comment string
	Body    string
}

type editorLoopConfig struct {
	tempFilePattern string

	nouns editorNouns

	// get shares the ETag with attempt, so a 412 re-fetch inside attempt updates it.
	get func(ctx context.Context) (body string, err error)

	// validate parses into the real type, so a wrongly shaped edit is caught before any request.
	validate func(editedBody string) error

	// attempt resolves a 412 itself: re-fetch, return the fresh document with conflictComment.
	attempt func(ctx context.Context, editedBody string) (editorAttemptResult, error)
}

// runEditorLoop is the kubectl-edit-style loop behind every `update --editor`.
func runEditorLoop(cmd *cobra.Command, cfg editorLoopConfig, editor editorFunc) error {
	ctx := cmd.Context()

	baseline, err := cfg.get(ctx)
	if err != nil {
		return err
	}

	tmpPath, err := createEditorTempFile(cfg.tempFilePattern, editorHeaderComment(cfg.nouns), baseline)
	if err != nil {
		return err
	}

	success := false

	defer func() {
		if success {
			_ = os.Remove(tmpPath)
			return
		}
		// Keep the temp file on every non-success exit so no work is lost.
		_, _ = fmt.Fprintln(cmd.ErrOrStderr(), tmpPath)
	}()

	for {
		if editErr := editor(tmpPath); editErr != nil {
			return fmt.Errorf("running editor: %w", editErr)
		}

		raw, readErr := os.ReadFile(tmpPath)
		if readErr != nil {
			return fmt.Errorf("reading %s: %w", tmpPath, readErr)
		}

		editedBody := strings.TrimSpace(stripCommentLines(string(raw)))
		if editedBody == "" || editedBody == strings.TrimSpace(baseline) {
			return &editCancelledError{}
		}

		if valErr := cfg.validate(editedBody); valErr != nil {
			if writeErr := writeEditorFile(tmpPath, invalidJSONComment(cfg.nouns, valErr), editedBody); writeErr != nil {
				return writeErr
			}

			// The reopened body is the new baseline, so saving it unchanged cancels instead of looping.
			baseline = editedBody

			continue
		}

		result, attemptErr := cfg.attempt(ctx, editedBody)
		if result.Done {
			// The PATCH was applied, so drop the temp file even if rendering failed.
			success = true
		}

		if attemptErr != nil {
			return attemptErr
		}

		if result.Done {
			return nil
		}

		if writeErr := writeEditorFile(tmpPath, result.Comment, result.Body); writeErr != nil {
			return writeErr
		}

		baseline = result.Body
	}
}

func createEditorTempFile(pattern, comment, body string) (string, error) {
	f, err := os.CreateTemp("", pattern)
	if err != nil {
		return "", fmt.Errorf("creating temp file: %w", err)
	}

	path := f.Name()

	if closeErr := f.Close(); closeErr != nil {
		return "", fmt.Errorf("creating temp file: %w", closeErr)
	}

	if err := writeEditorFile(path, comment, body); err != nil {
		return "", err
	}

	return path, nil
}

func writeEditorFile(path, comment, body string) error {
	content := comment + body
	if !strings.HasSuffix(content, "\n") {
		content += "\n"
	}

	if err := os.WriteFile(path, []byte(content), 0o600); err != nil {
		return fmt.Errorf("writing %s: %w", path, err)
	}

	return nil
}

func stripCommentLines(s string) string {
	lines := strings.Split(s, "\n")
	kept := lines[:0]

	for _, l := range lines {
		if strings.HasPrefix(strings.TrimSpace(l), "#") {
			continue
		}

		kept = append(kept, l)
	}

	return strings.Join(kept, "\n")
}

func commentBlock(lines ...string) string {
	var b strings.Builder

	for _, l := range lines {
		if l == "" {
			b.WriteString("#\n")
		} else {
			b.WriteString("# " + l + "\n")
		}
	}

	return b.String()
}

func editorHeaderComment(nouns editorNouns) string {
	return commentBlock(
		fmt.Sprintf("Editing %s.", nouns.Subject),
		"Lines starting with '#' are ignored.",
		"Save and exit to apply your changes; leave the JSON below unchanged to cancel.",
	)
}

func invalidJSONComment(nouns editorNouns, err error) string {
	return commentBlock(
		fmt.Sprintf("Could not parse the edited %s as JSON: %s", nouns.Noun, err),
		"Lines starting with '#' are ignored. Fix the JSON below and save again, or leave it unchanged to cancel.",
	)
}

// Plain values instead of api.ApiError keep editor.go independent of the api package.
func serverErrorComment(nouns editorNouns, message string, fieldErrors []string) string {
	lines := []string{fmt.Sprintf("The server rejected this %s: %s", nouns.Noun, message)}

	lines = append(lines, fieldErrors...)

	lines = append(lines, "Lines starting with '#' are ignored. Fix the JSON below and save again, or leave it unchanged to cancel.")

	return commentBlock(lines...)
}

// prettyJSON falls back to "{}"; only a broken MarshalJSON can fail.
func prettyJSON(v any) string {
	b, err := json.MarshalIndent(v, "", "  ")
	if err != nil {
		return "{}"
	}

	return string(b)
}

func conflictComment(nouns editorNouns, previousEdit string) string {
	lines := []string{
		fmt.Sprintf("The %s changed on the server since you started editing (ETag mismatch).", nouns.Noun),
		fmt.Sprintf("The %s below is now the current one on the server. Your previous edit is preserved,", nouns.Noun),
		"commented out, below -- re-apply it if it's still needed.",
		"Lines starting with '#' are ignored.",
		"",
		"Your previous edit:",
	}

	lines = append(lines, strings.Split(previousEdit, "\n")...)

	return commentBlock(lines...)
}
