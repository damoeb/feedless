package cmd

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"strings"
	"testing"
)

const (
	testRepoID   = "22222222-2222-2222-2222-222222222222"
	testSourceID = "33333333-3333-3333-3333-333333333333"
)

// validFlowJSON is a ScrapeFlow with one action of each kind view's
// numbered action list documents in the brief: fetch, click, extract.
const validFlowJSON = `{"sequence":[
  {"fetch":{"get":{"url":{"literal":"https://example.com"}}}},
  {"click":{"element":{"xpath":{"value":"//button"}}}},
  {"extract":{"fragmentName":"items","selectorBased":{"fragmentName":"item","xpath":{"value":"//li"},"emit":["text"],"uniqueBy":"text"}}}
]}`

func sourceJSON(id, repoID, title string, errorsInSuccession int, lastError string) string {
	last := "null"
	if lastError != "" {
		b, _ := json.Marshal(lastError)
		last = string(b)
	}

	return `{
		"id": "` + id + `",
		"repositoryId": "` + repoID + `",
		"title": "` + title + `",
		"errorsInSuccession": ` + itoa(errorsInSuccession) + `,
		"lastErrorMessage": ` + last + `,
		"flow": ` + validFlowJSON + `
	}`
}

func itoa(n int) string {
	b, _ := json.Marshal(n)
	return string(b)
}

// --- source list ---

func TestSourceList_WithRepo_HitsRepoEndpoint_PassesMinErrorsInSuccession(t *testing.T) {
	var gotPath string
	var gotQuery url.Values

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` + sourceJSON(testSourceID, testRepoID, "Broken Source", 3, "boom") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "list", "-R", testRepoID, "--errored")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/repositories/"+testRepoID+"/sources" {
		t.Errorf("path = %q, want the per-repository sources endpoint", gotPath)
	}
	if gotQuery.Get("minErrorsInSuccession") != "1" {
		t.Errorf("minErrorsInSuccession = %q, want 1 for bare --errored", gotQuery.Get("minErrorsInSuccession"))
	}

	out := stdout.String()
	fields := strings.Split(strings.TrimRight(out, "\n"), "\t")
	if len(fields) != 5 {
		t.Fatalf("row fields = %v (len %d), want 5 (ID TITLE ERRORS LAST_RUN LAST_ERROR, no REPO with -R)", fields, len(fields))
	}
	if fields[0] != testSourceID || fields[1] != "Broken Source" || fields[2] != "3" || fields[3] != "-" || fields[4] != "boom" {
		t.Errorf("row = %v, unexpected", fields)
	}
}

func TestSourceList_Errored_WithValue(t *testing.T) {
	var gotQuery url.Values

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("source", "list", "-R", testRepoID, "--errored=5")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotQuery.Get("minErrorsInSuccession") != "5" {
		t.Errorf("minErrorsInSuccession = %q, want 5", gotQuery.Get("minErrorsInSuccession"))
	}
}

func TestSourceList_Errored_LessThanOne_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "list", "-R", testRepoID, "--errored=0")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error for --errored=0")
	}
	if !strings.Contains(err.Error(), ">= 1") {
		t.Errorf("error = %v, want it to mention >= 1", err)
	}
}

func TestSourceList_WithoutRepo_HitsUserSourcesEndpoint_AndAddsREPOColumn(t *testing.T) {
	var gotPath string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` + sourceJSON(testSourceID, testRepoID, "Some Source", 0, "") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "list")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/user/sources" {
		t.Errorf("path = %q, want /api/v1/user/sources", gotPath)
	}

	fields := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\t")
	if len(fields) != 6 {
		t.Fatalf("row fields = %v (len %d), want 6 (ID TITLE REPO ERRORS LAST_RUN LAST_ERROR without -R)", fields, len(fields))
	}
	if fields[2] != testRepoID {
		t.Errorf("REPO column = %q, want %q", fields[2], testRepoID)
	}
}

func TestSourceList_JSON_Fields(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` + sourceJSON(testSourceID, testRepoID, "Some Source", 2, "boom") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "list", "-R", testRepoID, "--json=id,title,errorsInSuccession")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var rows []map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &rows); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if len(rows) != 1 || rows[0]["id"] != testSourceID || rows[0]["title"] != "Some Source" {
		t.Errorf("rows = %v, unexpected", rows)
	}
	if _, ok := rows[0]["tags"]; ok {
		t.Errorf("rows[0] = %v, want narrowed to the requested fields only", rows[0])
	}
}

func TestSourceList_BareJSON_ListsFields(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "list", "-R", testRepoID, "--json")
	if err == nil {
		t.Fatal("Execute() error = nil, want a FieldsError for bare --json")
	}
	if !strings.Contains(err.Error(), "errorsInSuccession") {
		t.Errorf("error = %v, want it to list the field names", err)
	}
}

// --- source view ---

func TestSourceView_RendersActionListAndFields(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet || r.URL.Path != "/api/v1/repositories/"+testRepoID+"/sources/"+testSourceID {
			t.Errorf("request = %s %s, want GET of the source", r.Method, r.URL.Path)
		}
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("ETag", `"abc123"`)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "My Source", 4, "network is down\nsecond line")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "view", testSourceID, "-R", testRepoID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	for _, want := range []string{
		"Title: My Source",
		"Repository: " + testRepoID,
		"Errors in succession: 4",
		"Last error: network is down\nsecond line",
		"1. fetch https://example.com",
		"2. click //button",
		"3. extract fragment items xpath //li",
	} {
		if !strings.Contains(out, want) {
			t.Errorf("stdout = %q, want it to contain %q", out, want)
		}
	}
}

func TestSourceView_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "view", testSourceID)
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}

func TestSourceView_JSON_PrintsSource(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "My Source", 1, "boom")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "view", testSourceID, "-R", testRepoID, "--json=id,flow")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var row map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &row); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if row["id"] != testSourceID {
		t.Errorf("row[id] = %v, want %q", row["id"], testSourceID)
	}
	if _, ok := row["flow"]; !ok {
		t.Errorf("row = %v, want a flow key (view's --json includes the full source)", row)
	}
}

func TestSourceView_404(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "view", testSourceID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a 404 error")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error = %v, want it to say not found", err)
	}
}

// --- source update (field flags / --flow, non-editor path) ---

func TestSourceUpdate_FieldFlags_NoIfMatch(t *testing.T) {
	var gotIfMatch string
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPatch {
			t.Errorf("method = %s, want PATCH", r.Method)
		}
		gotIfMatch = r.Header.Get("If-Match")
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("ETag", `"new-etag"`)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "Updated Title", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "update", testSourceID, "-R", testRepoID, "--title", "Updated Title", "--tags", "a, b")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotIfMatch != "" {
		t.Errorf("If-Match = %q, want empty (no conditional update from field flags)", gotIfMatch)
	}
	if gotBody["title"] != "Updated Title" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "Updated Title")
	}
	tags, _ := gotBody["tags"].([]any)
	if len(tags) != 2 || tags[0] != "a" || tags[1] != "b" {
		t.Errorf("body[tags] = %v, want [a b]", gotBody["tags"])
	}
	if _, hasFlow := gotBody["flow"]; hasFlow {
		t.Errorf("body = %v, want no flow key (no --flow given)", gotBody)
	}
	if !strings.Contains(stdout.String(), "Title: Updated Title") {
		t.Errorf("stdout = %q, want the updated source printed like `view`", stdout.String())
	}
}

func TestSourceUpdate_FlowFromFile(t *testing.T) {
	dir := t.TempDir()
	path := dir + "/flow.json"
	writeFile(t, path, validFlowJSON)

	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("source", "update", testSourceID, "-R", testRepoID, "--flow", path)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if _, ok := gotBody["flow"]; !ok {
		t.Errorf("body = %v, want a flow key from --flow", gotBody)
	}
}

func TestSourceUpdate_FlowFromStdin(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sourceJSON(testSourceID, testRepoID, "T", 0, "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmdWithStdin(validFlowJSON, "source", "update", testSourceID, "-R", testRepoID, "--flow", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if _, ok := gotBody["flow"]; !ok {
		t.Errorf("body = %v, want a flow key from --flow -", gotBody)
	}
}

func TestSourceUpdate_FlowInvalidJSON_LocalErrorBeforeAnyRequest(t *testing.T) {
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

	_, _, err := runCmd("source", "update", testSourceID, "-R", testRepoID, "--flow", path)
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid-JSON error")
	}
	if called {
		t.Error("server was called, want the invalid --flow JSON caught locally first")
	}
}

func TestSourceUpdate_EditorAndFlow_MutuallyExclusive(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "update", testSourceID, "-R", testRepoID, "--editor", "--flow", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error")
	}
	if !strings.Contains(err.Error(), "mutually exclusive") {
		t.Errorf("error = %v, want it to mention mutually exclusive", err)
	}
}

func TestSourceUpdate_NothingToUpdate_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "update", testSourceID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a nothing-to-update error")
	}
	if !strings.Contains(err.Error(), "nothing to update") {
		t.Errorf("error = %v, want it to mention nothing to update", err)
	}
}

func TestSourceUpdate_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "update", testSourceID, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}

func TestSourceUpdate_400_NonEditorPath_Fails(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		_, _ = w.Write([]byte(`{"code":"BAD_REQUEST","message":"invalid flow"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "update", testSourceID, "-R", testRepoID, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want the server's 400 surfaced")
	}
	if !strings.Contains(err.Error(), "invalid flow") {
		t.Errorf("error = %v, want the server's message", err)
	}
}

// --- helpers ---

func writeFile(t *testing.T, path, content string) {
	t.Helper()

	if err := os.WriteFile(path, []byte(content), 0o600); err != nil {
		t.Fatalf("writing %s: %v", path, err)
	}
}
