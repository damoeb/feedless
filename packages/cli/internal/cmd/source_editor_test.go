package cmd

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/client"
)

// otherFlowJSON, thirdFlowJSON: alternative valid ScrapeFlow bodies, distinct
// from validFlowJSON (source_test.go) and from each other, so tests can
// prove a fake editor's successive rewrites are actually round-tripped.
const (
	otherFlowJSON  = `{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.com/other"}}}}]}`
	thirdFlowJSON  = `{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.com/third"}}}}]}`
	fourthFlowJSON = `{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.com/fourth"}}}}]}`
)

// newEditorTestCmd builds a bare *cobra.Command wired the way runFlowEditor
// needs (Context/Out/Err) without going through cobra's Execute — the
// editor loop is tested as its own unit, independent of flag parsing,
// config.Load, and the keyring (per the brief: "make the editor injectable
// ... and must not touch the real keyring").
func newEditorTestCmd() (*cobra.Command, *bytes.Buffer, *bytes.Buffer) {
	c := &cobra.Command{}
	c.SetContext(context.Background())

	var stdout, stderr bytes.Buffer
	c.SetOut(&stdout)
	c.SetErr(&stderr)

	return c, &stdout, &stderr
}

func newEditorTestClient(t *testing.T, srvURL string) *client.Client {
	t.Helper()

	c, err := client.New("test-host", srvURL, "tok", "1.0.0", io.Discard, nil)
	if err != nil {
		t.Fatalf("client.New() error = %v", err)
	}

	return c
}

func mustUUID(t *testing.T, s string) uuid.UUID {
	t.Helper()

	id, err := uuid.Parse(s)
	if err != nil {
		t.Fatalf("uuid.Parse(%q) error = %v", s, err)
	}

	return id
}

// rewriteEditor returns an editorFunc that overwrites the file with content
// every time it's called — a fake editor script/function per the brief.
func rewriteEditor(content string) editorFunc {
	return func(path string) error {
		return os.WriteFile(path, []byte(content), 0o600)
	}
}

func noopEditor(_ string) error { return nil }

func tempFileExists(t *testing.T, path string) bool {
	t.Helper()

	_, err := os.Stat(path)
	if err == nil {
		return true
	}
	if errors.Is(err, os.ErrNotExist) {
		return false
	}

	t.Fatalf("os.Stat(%s) error = %v", path, err)

	return false
}

// findTempFilePath extracts the only line source_editor.go's runFlowEditor
// prints on any non-success exit path: the kept temp file's path.
func findTempFilePath(t *testing.T, stderr string) string {
	t.Helper()

	for _, line := range strings.Split(stderr, "\n") {
		if strings.Contains(line, "feedctl-source-"+testSourceID) {
			return strings.TrimSpace(line)
		}
	}

	t.Fatalf("stderr = %q, want a line with the kept temp file's path", stderr)

	return ""
}

func TestFlowEditor_Success_PrintsViewAndDeletesTempFile(t *testing.T) {
	var getCalls, patchCalls int
	var gotIfMatch string
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			getCalls++
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		case http.MethodPatch:
			patchCalls++
			gotIfMatch = r.Header.Get("If-Match")
			_ = json.NewDecoder(r.Body).Decode(&gotBody)
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v2"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		default:
			t.Errorf("unexpected request %s %s", r.Method, r.URL.Path)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, stderr := newEditorTestCmd()

	var tmpPathSeen string
	editor := func(path string) error {
		tmpPathSeen = path
		return os.WriteFile(path, []byte(otherFlowJSON), 0o600)
	}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runFlowEditor() error = %v, stderr = %q", err, stderr.String())
	}

	if getCalls != 1 || patchCalls != 1 {
		t.Errorf("getCalls=%d patchCalls=%d, want 1 and 1", getCalls, patchCalls)
	}
	if gotIfMatch != `"v1"` {
		t.Errorf("If-Match = %q, want the GET's ETag %q", gotIfMatch, `"v1"`)
	}
	if _, ok := gotBody["flow"]; !ok {
		t.Errorf("PATCH body = %v, want a flow key", gotBody)
	}
	if !strings.Contains(stdout.String(), "Title: T") {
		t.Errorf("stdout = %q, want the updated source printed like view", stdout.String())
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed on success", stderr.String())
	}
	if tempFileExists(t, tmpPathSeen) {
		t.Errorf("temp file %s still exists after success, want it deleted", tmpPathSeen)
	}
}

func TestFlowEditor_UnchangedOrEmpty_CancelledExit2_KeepsTempFile(t *testing.T) {
	tests := []struct {
		name   string
		editor editorFunc
	}{
		{"unchanged", noopEditor},
		{"empty", rewriteEditor("")},
		{"blank", rewriteEditor("   \n\n  ")},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				w.Header().Set("Content-Type", "application/json")
				w.Header().Set("ETag", `"v1"`)
				w.WriteHeader(http.StatusOK)
				_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
			}))
			t.Cleanup(srv.Close)

			apiClient := newEditorTestClient(t, srv.URL)
			cmd, _, stderr := newEditorTestCmd()

			err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, tc.editor)

			var cancelled *editCancelledError
			if !errors.As(err, &cancelled) {
				t.Fatalf("runFlowEditor() error = %v, want *editCancelledError", err)
			}
			if cancelled.ExitCode() != ExitCancelled {
				t.Errorf("ExitCode() = %d, want %d", cancelled.ExitCode(), ExitCancelled)
			}

			var rendered bytes.Buffer
			cancelled.RenderError(&rendered)
			if rendered.String() != "Edit cancelled, no changes made.\n" {
				t.Errorf("RenderError() = %q, want the exact cancellation message", rendered.String())
			}

			path := findTempFilePath(t, stderr.String())
			if !tempFileExists(t, path) {
				t.Errorf("temp file %s missing, want it kept on cancellation", path)
			}
		})
	}
}

func TestFlowEditor_InvalidJSON_ReopenedWithErrorComment(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		case http.MethodPatch:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v2"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newEditorTestCmd()

	calls := 0
	sawComment := false
	sawUserTextIntact := false

	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte("{ this is not valid json"), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "Could not parse the edited flow as JSON") {
			sawComment = true
		}
		if strings.Contains(content, "{ this is not valid json") {
			sawUserTextIntact = true
		}

		return os.WriteFile(path, []byte(otherFlowJSON), 0o600)
	}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runFlowEditor() error = %v", err)
	}
	if calls != 2 {
		t.Errorf("editor called %d times, want 2 (reopened once on invalid JSON)", calls)
	}
	if !sawComment {
		t.Error("reopened file did not contain the parse-error comment block")
	}
	if !sawUserTextIntact {
		t.Error("reopened file did not preserve the user's invalid text intact")
	}
}

func TestFlowEditor_400_ReopenedWithServerError(t *testing.T) {
	patchCalls := 0

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		case http.MethodPatch:
			patchCalls++
			if patchCalls == 1 {
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusBadRequest)
				_, _ = w.Write([]byte(`{"code":"BAD_REQUEST","message":"invalid flow",` +
					`"errors":[{"field":"sequence[0].fetch","message":"url must not be blank"}]}`))

				return
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newEditorTestCmd()

	calls := 0
	sawMessage := false
	sawFieldError := false

	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte(otherFlowJSON), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "The server rejected this flow: invalid flow") {
			sawMessage = true
		}
		if strings.Contains(content, "sequence[0].fetch: url must not be blank") {
			sawFieldError = true
		}

		return os.WriteFile(path, []byte(thirdFlowJSON), 0o600)
	}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runFlowEditor() error = %v", err)
	}
	if patchCalls != 2 {
		t.Errorf("PATCH called %d times, want 2 (reopened once on 400)", patchCalls)
	}
	if !sawMessage {
		t.Error("reopened file did not contain the server's error message")
	}
	if !sawFieldError {
		t.Error("reopened file did not contain the field error")
	}
}

func TestFlowEditor_412_RefetchesAndReopensWithFreshFlowAndCommentedEdit_SecondSaveSucceeds(t *testing.T) {
	getCalls := 0
	patchCalls := 0
	var ifMatches []string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			getCalls++
			w.Header().Set("Content-Type", "application/json")

			if getCalls == 1 {
				w.Header().Set("ETag", `"v1"`)
				w.WriteHeader(http.StatusOK)
				_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))

				return
			}

			// Second GET (the 412 re-fetch): a different flow, as if
			// someone else saved in between.
			w.Header().Set("ETag", `"v2"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(`{"id":"` + testSourceID + `","repositoryId":"` + testRepoID + `","title":"T",` +
				`"errorsInSuccession":0,"lastErrorMessage":null,"flow":` + thirdFlowJSON + `}`))
		case http.MethodPatch:
			patchCalls++
			ifMatches = append(ifMatches, r.Header.Get("If-Match"))

			if patchCalls == 1 {
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusPreconditionFailed)
				_, _ = w.Write([]byte(`{"code":"PRECONDITION_FAILED","message":"stale"}`))

				return
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, _ := newEditorTestCmd()

	calls := 0
	sawConflictNotice := false
	sawFreshFlow := false
	sawCommentedPreviousEdit := false

	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte(otherFlowJSON), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "ETag mismatch") {
			sawConflictNotice = true
		}
		if strings.Contains(content, "example.com/third") {
			sawFreshFlow = true
		}
		if strings.Contains(content, "# {") && strings.Contains(content, "example.com/other") {
			sawCommentedPreviousEdit = true
		}

		// Replace with a fourth, distinct flow so the retry isn't a no-op
		// cancellation against the fresh baseline just written.
		return os.WriteFile(path, []byte(fourthFlowJSON), 0o600)
	}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runFlowEditor() error = %v", err)
	}

	if getCalls != 2 {
		t.Errorf("GET called %d times, want 2 (initial + 412 re-fetch)", getCalls)
	}
	if patchCalls != 2 {
		t.Errorf("PATCH called %d times, want 2 (stale + retry)", patchCalls)
	}
	if len(ifMatches) != 2 || ifMatches[0] != `"v1"` || ifMatches[1] != `"v2"` {
		t.Errorf("If-Match sequence = %v, want [%q %q]", ifMatches, `"v1"`, `"v2"`)
	}
	if !sawConflictNotice {
		t.Error("reopened file did not explain the ETag mismatch")
	}
	if !sawFreshFlow {
		t.Error("reopened file's editable content was not the fresh flow from the server")
	}
	if !sawCommentedPreviousEdit {
		t.Error("reopened file did not preserve the user's previous edit, commented out")
	}
	if !strings.Contains(stdout.String(), "Title: T") {
		t.Errorf("stdout = %q, want the second save's success view", stdout.String())
	}
}

func TestFlowEditor_HardFailureAfterPatch_KeepsTempFile(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		case http.MethodPatch:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = w.Write([]byte(`{"code":"INTERNAL_ERROR","message":"boom"}`))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, stderr := newEditorTestCmd()

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, rewriteEditor(otherFlowJSON))
	if err == nil {
		t.Fatal("runFlowEditor() error = nil, want the 500 surfaced")
	}
	if !strings.Contains(err.Error(), "boom") {
		t.Errorf("error = %v, want the server's message", err)
	}

	path := findTempFilePath(t, stderr.String())
	if !tempFileExists(t, path) {
		t.Errorf("temp file %s missing, want it kept after a hard failure", path)
	}
}

func TestFlowEditor_EditorLaunchFailure_KeepsTempFile(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("ETag", `"v1"`)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, stderr := newEditorTestCmd()

	boom := errors.New("editor exploded")
	editor := func(_ string) error { return boom }

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err == nil || !strings.Contains(err.Error(), "editor exploded") {
		t.Fatalf("runFlowEditor() error = %v, want it to wrap the editor's own error", err)
	}

	path := findTempFilePath(t, stderr.String())
	if !tempFileExists(t, path) {
		t.Errorf("temp file %s missing, want it kept after an editor launch failure", path)
	}
}

func TestFlowEditor_FieldOverrides_CarriedIntoPatch(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
		case http.MethodPatch:
			_ = json.NewDecoder(r.Body).Decode(&gotBody)
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "New Title", 0, "")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newEditorTestCmd()

	title := "New Title"
	overrides := sourceFieldOverrides{title: &title}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), overrides, rewriteEditor(otherFlowJSON))
	if err != nil {
		t.Fatalf("runFlowEditor() error = %v", err)
	}

	if gotBody["title"] != "New Title" {
		t.Errorf("PATCH body[title] = %v, want %q (the --title override carried into the same PATCH)", gotBody["title"], "New Title")
	}
}

func TestFlowEditor_InitialGetFails_NoTempFileCreated(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, stderr := newEditorTestCmd()

	editorCalled := false
	editor := func(_ string) error {
		editorCalled = true
		return nil
	}

	err := runFlowEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), sourceFieldOverrides{}, editor)
	if err == nil {
		t.Fatal("runFlowEditor() error = nil, want the 404 surfaced")
	}
	if editorCalled {
		t.Error("editor was launched despite the initial GET failing")
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed (no temp file was ever created)", stderr.String())
	}
}
