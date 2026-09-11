package cmd

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

// --- source create ---

func TestSourceCreate_FromFlags(t *testing.T) {
	var gotPath string
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		if r.Method != http.MethodPost {
			t.Errorf("method = %s, want POST", r.Method)
		}
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "New Source", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	dir := t.TempDir()
	flowPath := dir + "/flow.json"
	writeFile(t, flowPath, validFlowJSON)

	stdout, stderr, err := runCmd("source", "create", "-R", testRepoID,
		"--title", "New Source", "--tags", "a,b", "--flow", flowPath)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/repositories/"+testRepoID+"/sources" {
		t.Errorf("path = %q, want /api/v1/repositories/%s/sources", gotPath, testRepoID)
	}
	if gotBody["title"] != "New Source" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "New Source")
	}
	tags, ok := gotBody["tags"].([]any)
	if !ok || len(tags) != 2 || tags[0] != "a" || tags[1] != "b" {
		t.Errorf("body[tags] = %v, want [a b]", gotBody["tags"])
	}
	if _, ok := gotBody["flow"]; !ok {
		t.Errorf("body = %v, want a flow key from --flow", gotBody)
	}
	if !strings.Contains(stdout.String(), "Title: New Source") {
		t.Errorf("stdout = %q, want the created source printed like `source view`", stdout.String())
	}
}

func TestSourceCreate_FromFlags_FlowFromStdin(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "From Stdin", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmdWithStdin(validFlowJSON, "source", "create", "-R", testRepoID,
		"--title", "From Stdin", "--flow", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "From Stdin" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "From Stdin")
	}
}

func TestSourceCreate_FromInput(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "From Input", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	input := `{"title":"From Input","flow":` + validFlowJSON + `}`

	stdout, stderr, err := runCmdWithStdin(input, "source", "create", "-R", testRepoID, "--input", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "From Input" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "From Input")
	}
	if !strings.Contains(stdout.String(), "Title: From Input") {
		t.Errorf("stdout = %q, want the created source printed like `source view`", stdout.String())
	}
}

func TestSourceCreate_MissingTitle_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "create", "-R", testRepoID, "--flow", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want --title required")
	}
	if !strings.Contains(err.Error(), "--title") {
		t.Errorf("error = %v, want it to mention --title", err)
	}
}

func TestSourceCreate_MissingFlow_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "create", "-R", testRepoID, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want --flow required")
	}
	if !strings.Contains(err.Error(), "--flow") {
		t.Errorf("error = %v, want it to mention --flow", err)
	}
}

func TestSourceCreate_InvalidFlowJSON_LocalErrorBeforeAnyRequest(t *testing.T) {
	dir := t.TempDir()
	path := dir + "/flow.json"
	writeFile(t, path, "{not json")

	called := false
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "create", "-R", testRepoID, "--title", "x", "--flow", path)
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid-JSON error")
	}
	if called {
		t.Error("server was called, want the invalid --flow JSON caught locally first")
	}
}

func TestSourceCreate_FlagsAndInput_MutuallyExclusive(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "create", "-R", testRepoID, "--title", "x", "--input", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error")
	}
	if !strings.Contains(err.Error(), "mutually exclusive") {
		t.Errorf("error = %v, want it to mention mutually exclusive", err)
	}
}

func TestSourceCreate_JSONOutput(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "JSON Source", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmdWithStdin(validFlowJSON, "source", "create", "-R", testRepoID,
		"--title", "JSON Source", "--flow", "-", "--json=id,title,flow")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var got map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &got); err != nil {
		t.Fatalf("stdout is not JSON: %v (%q)", err, stdout.String())
	}
	if got["title"] != "JSON Source" {
		t.Errorf("json[title] = %v, want %q", got["title"], "JSON Source")
	}
	if _, ok := got["flow"]; !ok {
		t.Errorf("json = %v, want a flow key", got)
	}
}

// --- source delete ---

func TestSourceDelete_Yes_DeletesWithoutPrompt_NoGET(t *testing.T) {
	var sawGet, sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			sawGet = true
			w.WriteHeader(http.StatusOK)
		case http.MethodDelete:
			sawDelete = true
			if r.URL.Path != "/api/v1/repositories/"+testRepoID+"/sources/"+testSourceID {
				t.Errorf("DELETE path = %q, want the source's path", r.URL.Path)
			}
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "delete", testSourceID, "-R", testRepoID, "--yes")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}
	if sawGet {
		t.Error("GET called despite --yes, want the title-fetching GET skipped")
	}
	if !sawDelete {
		t.Error("DELETE was never called")
	}
	if stdout.String() != "deleted "+testSourceID+"\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "deleted "+testSourceID+"\n")
	}
}

func TestSourceDelete_NotTTY_NoYes_Refused(t *testing.T) {
	called := false

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "delete", testSourceID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a refusal")
	}
	if err.Error() != "--yes is required when not running interactively" {
		t.Errorf("error = %q, want the exact brief message", err.Error())
	}
	if called {
		t.Error("server was called, want the non-TTY refusal to happen before any request")
	}
}

func TestSourceDelete_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "delete", testSourceID, "--yes")
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}

func TestSourceDelete_404_NotFound(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"nope"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "delete", testSourceID, "-R", testRepoID, "--yes")
	if err == nil {
		t.Fatal("Execute() error = nil, want not found")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error = %v, want it to mention not found", err)
	}
}

func TestDeleteSource_TTY_Accepted_FetchesTitleAndDeletes(t *testing.T) {
	var sawGet, sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			sawGet = true
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "My Source", 0, "")))
		case http.MethodDelete:
			sawDelete = true
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, stderr := newConfirmTestCmd("y\n")

	err := deleteSource(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), true, false)
	if err != nil {
		t.Fatalf("deleteSource() error = %v, stderr = %q", err, stderr.String())
	}
	if !sawGet {
		t.Error("GET was never called to fetch the title for the prompt")
	}
	if !sawDelete {
		t.Error("DELETE was never called")
	}
	if !strings.Contains(stderr.String(), "Delete source My Source ("+testSourceID+")? [y/N] ") {
		t.Errorf("stderr = %q, want the exact confirmation prompt naming the fetched title", stderr.String())
	}
	if stdout.String() != "deleted "+testSourceID+"\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "deleted "+testSourceID+"\n")
	}
}

func TestDeleteSource_TTY_NoYes_TitleGET404_NotFound_NoPromptNoDelete(t *testing.T) {
	var sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"nope"}`))
		case http.MethodDelete:
			sawDelete = true
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, stderr := newConfirmTestCmd("y\n")

	err := deleteSource(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), true, false)

	var apiErr *APIError
	if !errors.As(err, &apiErr) {
		t.Fatalf("deleteSource() error = %v, want *APIError", err)
	}
	if !strings.Contains(apiErr.Error(), "not found") {
		t.Errorf("error = %v, want it to mention not found", apiErr)
	}
	if apiErr.ExitCode() != 1 {
		t.Errorf("ExitCode() = %d, want 1", apiErr.ExitCode())
	}
	if strings.Contains(stderr.String(), "Delete source") {
		t.Errorf("stderr = %q, want no prompt written when the title GET 404s", stderr.String())
	}
	if sawDelete {
		t.Error("DELETE was called despite the title GET returning 404")
	}
}

func TestDeleteSource_TTY_Declined_ExitCode2_NoDelete(t *testing.T) {
	var sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "My Source", 0, "")))
		case http.MethodDelete:
			sawDelete = true
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newConfirmTestCmd("n\n")

	err := deleteSource(cmd, apiClient, mustUUID(t, testRepoID), mustUUID(t, testSourceID), true, false)

	var declined *deleteDeclinedError
	if !errors.As(err, &declined) {
		t.Fatalf("deleteSource() error = %v, want *deleteDeclinedError", err)
	}
	if declined.ExitCode() != ExitCancelled {
		t.Errorf("ExitCode() = %d, want %d", declined.ExitCode(), ExitCancelled)
	}
	if sawDelete {
		t.Error("DELETE was called despite the prompt being declined")
	}
}
