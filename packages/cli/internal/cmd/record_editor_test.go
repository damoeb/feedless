package cmd

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"
)

func findRecordTempFilePath(t *testing.T, stderr string) string {
	t.Helper()

	for _, line := range strings.Split(stderr, "\n") {
		if strings.Contains(line, "feedctl-record-"+testRecordID) {
			return strings.TrimSpace(line)
		}
	}

	t.Fatalf("stderr = %q, want a line with the kept temp file's path", stderr)

	return ""
}

func TestRecordEditor_Success_PrintsViewAndDeletesTempFile(t *testing.T) {
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
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
		case http.MethodPatch:
			patchCalls++
			gotIfMatch = r.Header.Get("If-Match")
			_ = json.NewDecoder(r.Body).Decode(&gotBody)
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v2"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "New Title")))
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

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}
		if !strings.Contains(string(raw), `"title": "T"`) {
			t.Errorf("initial temp file = %q, want the current title prefilled", raw)
		}
		if !strings.Contains(string(raw), `"url": "https://example.com/a"`) {
			t.Errorf("initial temp file = %q, want the current url prefilled", raw)
		}

		return os.WriteFile(path, []byte(`{"title":"New Title","url":"https://example.com/a","text":"body text","tags":["a","b"]}`), 0o600)
	}

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runRecordEditor() error = %v, stderr = %q", err, stderr.String())
	}

	if getCalls != 1 || patchCalls != 1 {
		t.Errorf("getCalls=%d patchCalls=%d, want 1 and 1", getCalls, patchCalls)
	}
	if gotIfMatch != `"v1"` {
		t.Errorf("If-Match = %q, want the GET's ETag %q", gotIfMatch, `"v1"`)
	}
	if gotBody["title"] != "New Title" {
		t.Errorf("PATCH body[title] = %v, want %q", gotBody["title"], "New Title")
	}
	if !strings.Contains(stdout.String(), "Title: New Title") {
		t.Errorf("stdout = %q, want the updated record printed like view", stdout.String())
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed on success", stderr.String())
	}
	if _, statErr := os.Stat(tmpPathSeen); !errors.Is(statErr, os.ErrNotExist) {
		t.Errorf("temp file %s still exists after success, want it deleted", tmpPathSeen)
	}
}

func TestRecordEditor_UnchangedOrEmpty_CancelledExit2_KeepsTempFile(t *testing.T) {
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
				_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
			}))
			t.Cleanup(srv.Close)

			apiClient := newEditorTestClient(t, srv.URL)
			cmd, _, stderr := newEditorTestCmd()

			err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, tc.editor)

			var cancelled *editCancelledError
			if !errors.As(err, &cancelled) {
				t.Fatalf("runRecordEditor() error = %v, want *editCancelledError", err)
			}
			if cancelled.ExitCode() != ExitCancelled {
				t.Errorf("ExitCode() = %d, want %d", cancelled.ExitCode(), ExitCancelled)
			}

			path := findRecordTempFilePath(t, stderr.String())
			if !tempFileExists(t, path) {
				t.Errorf("temp file %s missing, want it kept on cancellation", path)
			}
		})
	}
}

func TestRecordEditor_InvalidJSON_ReopenedWithErrorComment(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
		case http.MethodPatch:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
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
		if strings.Contains(content, "Could not parse the edited record as JSON") {
			sawComment = true
		}
		if strings.Contains(content, "{ this is not valid json") {
			sawUserTextIntact = true
		}

		return os.WriteFile(path, []byte(`{"title":"Fixed","url":"https://example.com/a"}`), 0o600)
	}

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runRecordEditor() error = %v", err)
	}
	if calls != 2 {
		t.Errorf("editor called %d times, want 2 (reopened once on invalid JSON)", calls)
	}
	if !sawComment {
		t.Error("reopened file did not contain the parse-error comment block naming 'record'")
	}
	if !sawUserTextIntact {
		t.Error("reopened file did not preserve the user's invalid text intact")
	}
}

func TestRecordEditor_400_ReopenedWithServerError(t *testing.T) {
	patchCalls := 0

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
		case http.MethodPatch:
			patchCalls++
			if patchCalls == 1 {
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusBadRequest)
				_, _ = w.Write([]byte(`{"code":"BAD_REQUEST","message":"invalid url",` +
					`"errors":[{"field":"url","message":"must not be blank"}]}`))

				return
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/fixed", "Fixed")))
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
			return os.WriteFile(path, []byte(`{"url":""}`), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "The server rejected this record: invalid url") {
			sawMessage = true
		}
		if strings.Contains(content, "url: must not be blank") {
			sawFieldError = true
		}

		return os.WriteFile(path, []byte(`{"title":"Fixed","url":"https://example.com/fixed"}`), 0o600)
	}

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runRecordEditor() error = %v", err)
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

func TestRecordEditor_412_RefetchesAndReopensWithFreshDocumentAndCommentedEdit_SecondSaveSucceeds(t *testing.T) {
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
				_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))

				return
			}

			w.Header().Set("ETag", `"v2"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "Fresh From Server")))
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
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "Final Title")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, _ := newEditorTestCmd()

	calls := 0
	sawConflictNotice := false
	sawFreshDocument := false
	sawCommentedPreviousEdit := false

	editor := func(path string) error {
		calls++
		if calls == 1 {
			return os.WriteFile(path, []byte(`{"title":"My Edit","url":"https://example.com/a"}`), 0o600)
		}

		raw, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		content := string(raw)
		if strings.Contains(content, "ETag mismatch") {
			sawConflictNotice = true
		}
		if strings.Contains(content, "Fresh From Server") {
			sawFreshDocument = true
		}
		if strings.Contains(content, "# {") && strings.Contains(content, "My Edit") {
			sawCommentedPreviousEdit = true
		}

		return os.WriteFile(path, []byte(`{"title":"Final Title","url":"https://example.com/a"}`), 0o600)
	}

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, editor)
	if err != nil {
		t.Fatalf("runRecordEditor() error = %v", err)
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
	if !sawFreshDocument {
		t.Error("reopened file's editable content was not the fresh document from the server")
	}
	if !sawCommentedPreviousEdit {
		t.Error("reopened file did not preserve the user's previous edit, commented out")
	}
	if !strings.Contains(stdout.String(), "Title: Final Title") {
		t.Errorf("stdout = %q, want the second save's success view", stdout.String())
	}
}

func TestRecordEditor_FieldOverrides_CarriedIntoPatch(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
		case http.MethodPatch:
			_ = json.NewDecoder(r.Body).Decode(&gotBody)
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "Override Title")))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newEditorTestCmd()

	title := "Override Title"
	overrides := recordFieldOverrides{title: &title}

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), overrides,
		rewriteEditor(`{"title":"Edited In Editor","url":"https://example.com/a"}`))
	if err != nil {
		t.Fatalf("runRecordEditor() error = %v", err)
	}

	if gotBody["title"] != "Override Title" {
		t.Errorf("PATCH body[title] = %v, want %q (the --title override wins over the edited JSON)", gotBody["title"], "Override Title")
	}
}

func TestRecordEditor_HardFailureAfterPatch_KeepsTempFile(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("ETag", `"v1"`)
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "T")))
		case http.MethodPatch:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = w.Write([]byte(`{"code":"INTERNAL_ERROR","message":"boom"}`))
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, stderr := newEditorTestCmd()

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{},
		rewriteEditor(`{"title":"X","url":"https://example.com/a"}`))
	if err == nil {
		t.Fatal("runRecordEditor() error = nil, want the 500 surfaced")
	}
	if !strings.Contains(err.Error(), "boom") {
		t.Errorf("error = %v, want the server's message", err)
	}

	path := findRecordTempFilePath(t, stderr.String())
	if !tempFileExists(t, path) {
		t.Errorf("temp file %s missing, want it kept after a hard failure", path)
	}
}

func TestRecordEditor_InitialGetFails_NoTempFileCreated(t *testing.T) {
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

	err := runRecordEditor(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testRecordID), recordFieldOverrides{}, editor)
	if err == nil {
		t.Fatal("runRecordEditor() error = nil, want the 404 surfaced")
	}
	if editorCalled {
		t.Error("editor was launched despite the initial GET failing")
	}
	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want nothing printed (no temp file was ever created)", stderr.String())
	}
}
