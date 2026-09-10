package cmd

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"strings"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
)

// editorFunc opens path in the user's editor and blocks until they exit —
// the seam `source update --editor` is injected through, so tests can
// supply a fake editor (a function that rewrites the file, or a script)
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

// editCancelledError is returned when the user's edit was empty or
// unchanged (requirement 3, step 3). It implements the same two
// extension-point interfaces *cmd.APIError does (see main.go's
// errorRenderer and exitCoder), so it goes through the exact same
// rendering/exit-code path: a plain "Edit cancelled, no changes made." line
// (no "error: " prefix — this isn't a failure, it's a deliberate no-op) and
// exit code 2 (cmd.ExitCancelled, the same code C3 reserved for exactly
// this case).
type editCancelledError struct{}

func (e *editCancelledError) Error() string { return "edit cancelled, no changes made" }

func (e *editCancelledError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintln(w, "Edit cancelled, no changes made.")
}

func (e *editCancelledError) ExitCode() int { return ExitCancelled }

// runFlowEditor is the `source update --editor` loop, kubectl-edit style
// (requirement 3): GET the source and its ETag, write the flow to a temp
// file, open it in editor, and on save either PATCH it (success), reopen
// the editor with a comment block describing what went wrong (invalid
// JSON, a 400 from the server, or a 412 conflict — re-fetching first), or
// stop because the edit was empty/unchanged. overrides carries
// --title/--tags/--disabled, folded into every PATCH attempt alongside the
// edited flow ("other flags may be combined with --editor; they go into
// the same PATCH").
//
// It is kept as its own small unit, independent of cobra flag parsing and
// of client.NewFromConfig/config.Load, so it can be exercised directly
// against an httptest server with a fake editorFunc — see
// source_editor_test.go.
func runFlowEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	overrides sourceFieldOverrides, editor editorFunc,
) error {
	ctx := cmd.Context()

	resp, err := apiClient.API.GetSourceWithResponse(ctx, repoID, sourceID)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
		return apiErr
	}

	etag := etagOf(resp.Headers200)

	baseline := prettyFlow(resp.JSON200.Flow)

	tmpPath, err := createFlowTempFile(sourceID, editorHeaderComment(sourceID), baseline)
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
		// print its path so no work is lost" (requirement 3, step 6) —
		// applies uniformly, including the cancelled-edit case.
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

		var flow api.ScrapeFlow
		if jsonErr := json.Unmarshal([]byte(editedBody), &flow); jsonErr != nil {
			if writeErr := writeFlowFile(tmpPath, invalidJSONComment(jsonErr), editedBody); writeErr != nil {
				return writeErr
			}

			// Mirrors the 400/412 branches below: the reopened file's body
			// becomes the new baseline, so saving it back unchanged cancels
			// the edit (per invalidJSONComment's own "leave it unchanged to
			// cancel" instruction) instead of looping forever.
			baseline = editedBody

			continue
		}

		patch := api.SourceUpdate{Title: overrides.title, Tags: overrides.tags, Disabled: overrides.disabled, Flow: &flow}

		var ifMatch *api.IfMatch
		if etag != "" {
			e := etag
			ifMatch = &e
		}

		updResp, patchErr := apiClient.API.UpdateSourceWithResponse(ctx, repoID, sourceID, &api.UpdateSourceParams{IfMatch: ifMatch}, patch)
		if patchErr != nil {
			return patchErr
		}

		switch updResp.StatusCode() {
		case http.StatusOK:
			success = true

			return renderSourceView(cmd.OutOrStdout(), *updResp.JSON200)

		case http.StatusBadRequest:
			var serverErr api.ApiError
			_ = json.Unmarshal(updResp.Body, &serverErr)

			if writeErr := writeFlowFile(tmpPath, serverErrorComment(serverErr), editedBody); writeErr != nil {
				return writeErr
			}

			baseline = editedBody

		case http.StatusPreconditionFailed:
			reGet, getErr := apiClient.API.GetSourceWithResponse(ctx, repoID, sourceID)
			if getErr != nil {
				return getErr
			}
			if apiErr := NewAPIError(reGet, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
				return apiErr
			}

			etag = etagOf(reGet.Headers200)

			fresh := prettyFlow(reGet.JSON200.Flow)
			if writeErr := writeFlowFile(tmpPath, conflictComment(editedBody), fresh); writeErr != nil {
				return writeErr
			}

			baseline = fresh

		default:
			return NewAPIError(updResp, fmt.Sprintf("source %s", sourceID))
		}
	}
}

func etagOf(headers *api.GetSourceResponse200Headers) string {
	if headers == nil || headers.ETag == nil {
		return ""
	}

	return *headers.ETag
}

// createFlowTempFile creates the editor's temp file — named
// feedctl-source-<id>-*.json, per requirement 3 step 2 — with comment plus
// body as its content.
func createFlowTempFile(sourceID api.SourceId, comment, body string) (string, error) {
	f, err := os.CreateTemp("", fmt.Sprintf("feedctl-source-%s-*.json", sourceID))
	if err != nil {
		return "", fmt.Errorf("creating temp file: %w", err)
	}

	path := f.Name()

	if closeErr := f.Close(); closeErr != nil {
		return "", fmt.Errorf("creating temp file: %w", closeErr)
	}

	if err := writeFlowFile(path, comment, body); err != nil {
		return "", err
	}

	return path, nil
}

func writeFlowFile(path, comment, body string) error {
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
// '#' — "the tool's own comment block" (requirement 3, step 3).
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

func editorHeaderComment(sourceID api.SourceId) string {
	return commentBlock(
		fmt.Sprintf("Editing the flow for source %s.", sourceID),
		"Lines starting with '#' are ignored.",
		"Save and exit to apply your changes; leave the JSON below unchanged to cancel.",
	)
}

func invalidJSONComment(err error) string {
	return commentBlock(
		fmt.Sprintf("Could not parse the edited flow as JSON: %s", err),
		"Lines starting with '#' are ignored. Fix the JSON below and save again, or leave it unchanged to cancel.",
	)
}

func serverErrorComment(apiErr api.ApiError) string {
	lines := []string{fmt.Sprintf("The server rejected this flow: %s", apiErr.Message)}

	if apiErr.Errors != nil {
		for _, fe := range *apiErr.Errors {
			lines = append(lines, fmt.Sprintf("  %s: %s", fe.Field, fe.Message))
		}
	}

	lines = append(lines, "Lines starting with '#' are ignored. Fix the JSON below and save again, or leave it unchanged to cancel.")

	return commentBlock(lines...)
}

func conflictComment(previousEdit string) string {
	lines := []string{
		"The source changed on the server since you started editing (ETag mismatch).",
		"The flow below is now the current one on the server. Your previous edit is preserved,",
		"commented out, below -- re-apply it if it's still needed.",
		"Lines starting with '#' are ignored.",
		"",
		"Your previous edit:",
	}

	lines = append(lines, strings.Split(previousEdit, "\n")...)

	return commentBlock(lines...)
}
