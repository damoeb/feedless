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

// editorFunc opens path in the user's editor and blocks until they exit —
// the seam every `update --editor` command is injected through, so tests
// can supply a fake editor (a function that rewrites the file, or a script)
// instead of launching a real one. launchSystemEditor is the production
// implementation.
type editorFunc func(path string) error

// launchSystemEditor runs $VISUAL, else $EDITOR, else vi on path,
// connected to the process's own stdio so an interactive editor works
// normally. It runs the editor command through "sh -c" so an editor value
// containing its own arguments (e.g. "code --wait") works, the same way
// git/gh invoke $EDITOR.
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

// editCancelledError is returned by runEditorLoop when the user's edit was
// empty or unchanged. It implements the same two extension-point
// interfaces *cmd.APIError does (see main.go's errorRenderer and
// exitCoder), so it goes through the exact same rendering/exit-code path: a
// plain "Edit cancelled, no changes made." line (no "error: " prefix — this
// isn't a failure, it's a deliberate no-op) and exit code 2
// (cmd.ExitCancelled, the same code reserved for exactly this case).
type editCancelledError struct{}

func (e *editCancelledError) Error() string { return "edit cancelled, no changes made" }

func (e *editCancelledError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintln(w, "Edit cancelled, no changes made.")
}

func (e *editCancelledError) ExitCode() int { return ExitCancelled }

// editorNouns is the small set of resource-specific words runEditorLoop's
// comment-block builders need to read naturally, whatever's being edited:
//   - Subject: the ID-qualified description used only in the initial
//     header comment, e.g. "the flow for source <id>" (source update
//     --editor) or "repository <id>" (repo update --editor).
//   - Noun: the editable document's own name, used everywhere else —
//     "Could not parse the edited <Noun> as JSON", "The server rejected
//     this <Noun>", "The <Noun> changed on the server ...". "flow" for
//     source, "repository" for repo.
type editorNouns struct {
	Subject string
	Noun    string
}

// editorAttemptResult is what editorLoopConfig.attempt returns for one save
// attempt. Done true means the save succeeded and attempt has already
// rendered the result itself (to cmd.OutOrStdout()) — runEditorLoop deletes
// the temp file and returns nil. Done false means the attempt was rejected
// in a recoverable way (an invalid document per the server, or a stale
// ETag): Comment and Body become the reopened temp file's new comment
// header and editable content respectively, and Body becomes the next
// baseline against which "saved unchanged" is checked. A non-nil error from
// attempt is terminal: runEditorLoop keeps the temp file, prints its path,
// and returns the error unchanged (e.g. a 404/429/5xx via NewAPIError).
type editorAttemptResult struct {
	Done    bool
	Comment string
	Body    string
}

// editorLoopConfig bundles everything runEditorLoop needs to drive one
// kubectl-edit-style `update --editor` flow, independent of which resource
// is being edited — source update (source_editor.go's runFlowEditor) and
// repo update (repository_editor.go's runRepositoryEditor) each build one
// and hand it to runEditorLoop; no other command duplicates this loop.
type editorLoopConfig struct {
	// tempFilePattern is os.CreateTemp's pattern for the editor's temp
	// file, e.g. "feedctl-source-<id>-*.json" / "feedctl-repo-<id>-*.json".
	tempFilePattern string

	nouns editorNouns

	// get fetches the current editable document, pretty-printed as JSON,
	// and records its ETag internally (via a closure shared with attempt,
	// so a 412's re-fetch inside attempt updates the same state) — called
	// once, up front.
	get func(ctx context.Context) (body string, err error)

	// validate parses editedBody into whatever Go type actually represents
	// the document (api.ScrapeFlow, api.RepositoryUpdate, …), so a
	// syntactically valid but wrongly-shaped edit is still caught locally,
	// before any request — matching the original source-only behavior this
	// loop generalizes. A non-nil error reopens the editor with
	// invalidJSONComment.
	validate func(editedBody string) error

	// attempt sends one PATCH built from editedBody (already validated)
	// and whatever ETag/overrides the resource-specific closure is
	// tracking, and reports the outcome — see editorAttemptResult. A 412
	// conflict is attempt's own responsibility to resolve (re-fetch via
	// the same mechanism as get, returning the fresh document as Body and
	// conflictComment(nouns, editedBody) as Comment).
	attempt func(ctx context.Context, editedBody string) (editorAttemptResult, error)
}

// runEditorLoop is the resource-agnostic kubectl-edit loop shared by every
// entity's `update --editor`: GET (via cfg.get), write a temp file, open
// the editor, and on save either apply it (cfg.attempt reports Done),
// reopen the editor with an explanatory comment (invalid JSON — checked
// here via cfg.validate — or a recoverable rejection reported by
// cfg.attempt), or stop because the edit was empty/unchanged
// (*editCancelledError, exit 2). Kept independent of cobra flag parsing and
// of client.NewFromConfig/config.Load, so both source_editor_test.go and
// repository_editor_test.go can drive their resource-specific wrappers
// directly against an httptest server with a fake editorFunc — see also
// editor_test.go for tests of this loop's own generic behavior.
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
		// "On any exit path other than success, keep the temp file and
		// print its path so no work is lost" — applies uniformly,
		// including the cancelled-edit case.
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

			// Mirrors the reopen branches below: the reopened file's body
			// becomes the new baseline, so saving it back unchanged
			// cancels the edit instead of looping forever.
			baseline = editedBody

			continue
		}

		result, attemptErr := cfg.attempt(ctx, editedBody)
		if result.Done {
			// Matches the loop's original (pre-generalization) behavior:
			// a successful save always deletes the temp file, even if
			// attempt's own error is non-nil (e.g. a rendering failure
			// after an already-applied PATCH) — the update itself
			// succeeded.
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

// createEditorTempFile creates the editor's temp file with comment plus
// body as its content, named per pattern (os.CreateTemp's pattern
// argument).
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

// stripCommentLines drops every line whose first non-space character is
// '#' — the tool's own comment block.
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

// serverErrorComment takes message/fieldErrors as plain values rather than
// an api.ApiError directly, so editor.go stays decoupled from the api
// package: each resource-specific wrapper (source_editor.go,
// repository_editor.go) already has its own typed ApiError in scope for
// its 400 branch and renders fieldErrors itself (see fieldErrorLines).
func serverErrorComment(nouns editorNouns, message string, fieldErrors []string) string {
	lines := []string{fmt.Sprintf("The server rejected this %s: %s", nouns.Noun, message)}

	lines = append(lines, fieldErrors...)

	lines = append(lines, "Lines starting with '#' are ignored. Fix the JSON below and save again, or leave it unchanged to cancel.")

	return commentBlock(lines...)
}

// prettyJSON marshals v as indented JSON for an editor's temp file — the
// shared helper every `update --editor` wrapper uses to build its initial
// baseline document (source_editor.go's prettyFlow predates this and stays
// separate since it renders just the flow field, not a whole document; new
// resources — e.g. C7's record update --editor — should use this one
// directly). Falls back to "{}" on a marshal error (only reachable for a
// type with a broken MarshalJSON; every generated api.* type marshals
// cleanly).
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
