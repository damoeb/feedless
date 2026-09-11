package cmd

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strconv"
	"strings"
	"testing"
)

const (
	testRepoID2  = "55555555-5555-5555-5555-555555555555"
	testOwnerID  = "66666666-6666-6666-6666-666666666666"
	testRepoName = "My Repo"
)

func repositoryJSON(id, title, product, visibility, cron string, archived bool) string {
	return `{
		"id": "` + id + `",
		"title": "` + title + `",
		"description": "a description",
		"ownerId": "` + testOwnerID + `",
		"product": "` + product + `",
		"visibility": "` + visibility + `",
		"refreshCron": "` + cron + `",
		"tags": [],
		"createdAt": "2024-01-01T00:00:00Z",
		"lastUpdatedAt": "2024-01-02T00:00:00Z",
		"archived": ` + strconv.FormatBool(archived) + `
	}`
}

func TestRepositoryList_PassesFilters_AndRendersTable(t *testing.T) {
	var gotPath string
	var gotQuery url.Values

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` +
			repositoryJSON(testRepoID2, testRepoName, "feedless", "public", "0 * * * *", false) +
			`],"hasMore":false,"totalCount":1}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "list", "--product", "feedless", "--visibility", "public", "--search", "foo")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/repositories" {
		t.Errorf("path = %q, want /api/v1/repositories", gotPath)
	}
	if gotQuery.Get("product") != "feedless" {
		t.Errorf("product = %q, want feedless", gotQuery.Get("product"))
	}
	if gotQuery.Get("visibility") != "public" {
		t.Errorf("visibility = %q, want public", gotQuery.Get("visibility"))
	}
	if gotQuery.Get("q") != "foo" {
		t.Errorf("q = %q, want foo (--search maps to q)", gotQuery.Get("q"))
	}

	fields := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\t")
	if len(fields) != 6 {
		t.Fatalf("row fields = %v (len %d), want 6 (ID TITLE PRODUCT VISIBILITY CRON UPDATED)", fields, len(fields))
	}
	if fields[0] != testRepoID2 || fields[1] != testRepoName || fields[2] != "feedless" ||
		fields[3] != "public" || fields[4] != "0 * * * *" {
		t.Errorf("row = %v, unexpected", fields)
	}
}

func TestRepositoryList_JSON_Fields(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` +
			repositoryJSON(testRepoID2, testRepoName, "feedless", "public", "0 * * * *", false) +
			`],"hasMore":false,"totalCount":1}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "list", "--json=id,title,product")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var rows []map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &rows); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if len(rows) != 1 || rows[0]["id"] != testRepoID2 || rows[0]["title"] != testRepoName || rows[0]["product"] != "feedless" {
		t.Errorf("rows = %v, unexpected", rows)
	}
	if _, ok := rows[0]["visibility"]; ok {
		t.Errorf("rows[0] = %v, want narrowed to the requested fields only", rows[0])
	}
}

func TestRepositoryList_BareJSON_ListsFields(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "list", "--json")
	if err == nil {
		t.Fatal("Execute() error = nil, want a FieldsError for bare --json")
	}
	if !strings.Contains(err.Error(), "title") {
		t.Errorf("error = %v, want it to list the field names", err)
	}
}

func TestRepositoryList_Limit_Paginates(t *testing.T) {
	var pages int

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		pages++
		page := r.URL.Query().Get("page")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)

		if page == "0" {
			_, _ = w.Write([]byte(`{"items":[` +
				repositoryJSON(testRepoID2, "R1", "feedless", "public", "", false) +
				`],"hasMore":true,"totalCount":2}`))

			return
		}

		_, _ = w.Write([]byte(`{"items":[` +
			repositoryJSON(testRepoID, "R2", "feedless", "public", "", false) +
			`],"hasMore":false,"totalCount":2}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "list", "--limit", "2")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}
	if pages != 2 {
		t.Errorf("pages fetched = %d, want 2", pages)
	}

	lines := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\n")
	if len(lines) != 2 {
		t.Errorf("rows printed = %d, want 2", len(lines))
	}
}

func TestRepositoryView_RendersFields(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet || r.URL.Path != "/api/v1/repositories/"+testRepoID2 {
			t.Errorf("request = %s %s, want GET of the repository", r.Method, r.URL.Path)
		}
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("ETag", `"abc123"`)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(repositoryJSON(testRepoID2, testRepoName, "feedless", "private", "*/5 * * * *", true)))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "view", testRepoID2)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	for _, want := range []string{
		"Title: " + testRepoName,
		"Product: feedless",
		"Visibility: private",
		"Cron: */5 * * * *",
		"Retention: -",
		"Archived: true",
	} {
		if !strings.Contains(out, want) {
			t.Errorf("stdout = %q, want it to contain %q", out, want)
		}
	}
}

func TestRepositoryView_MaliciousTitleAndDescription_NoEscapeSequences(t *testing.T) {
	esc := string(rune(0x1b))
	maliciousTitle := esc + "[2Jhijacked title"
	maliciousDescription := "before " + esc + "]0;pwned" + string(rune(0x07)) + " after"

	// Built by hand: repositoryJSON doesn't escape, and raw control bytes need json.Marshal.
	titleJSON, err := json.Marshal(maliciousTitle)
	if err != nil {
		t.Fatalf("json.Marshal(title): %v", err)
	}

	descJSON, err := json.Marshal(maliciousDescription)
	if err != nil {
		t.Fatalf("json.Marshal(description): %v", err)
	}

	body := `{"id":"` + testRepoID2 + `","title":` + string(titleJSON) + `,"description":` + string(descJSON) +
		`,"ownerId":"` + testOwnerID + `","product":"feedless","visibility":"private","refreshCron":"",` +
		`"tags":[],"createdAt":"2024-01-01T00:00:00Z","lastUpdatedAt":"2024-01-02T00:00:00Z","archived":false}`

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(body))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "view", testRepoID2)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	if strings.ContainsRune(out, 0x1b) {
		t.Errorf("stdout = %q, want no ESC (0x1b) anywhere in the view", out)
	}
	if strings.ContainsRune(out, 0x07) {
		t.Errorf("stdout = %q, want no BEL (0x07) anywhere in the view", out)
	}
	if !strings.Contains(out, "Title: hijacked title") {
		t.Errorf("stdout = %q, want the sanitized title", out)
	}
	if !strings.Contains(out, "before") || !strings.Contains(out, "after") {
		t.Errorf("stdout = %q, want the text around the stripped OSC sequence preserved", out)
	}
}

func TestRepositoryView_JSON_PrintsRepository(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(repositoryJSON(testRepoID2, testRepoName, "feedless", "public", "", false)))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "view", testRepoID2, "--json=id,title,description")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var row map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &row); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if row["id"] != testRepoID2 {
		t.Errorf("row[id] = %v, want %q", row["id"], testRepoID2)
	}
	if _, ok := row["description"]; !ok {
		t.Errorf("row = %v, want a description key", row)
	}
}

func TestRepositoryView_404(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("repo", "view", testRepoID2)
	if err == nil {
		t.Fatal("Execute() error = nil, want a 404 error")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error = %v, want it to say not found", err)
	}
}

func TestRepositoryCreate_FromFlags(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost || r.URL.Path != "/api/v1/repositories" {
			t.Errorf("request = %s %s, want POST /api/v1/repositories", r.Method, r.URL.Path)
		}
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(repositoryJSON(testRepoID2, "New Repo", "feedless", "private", "0 0 * * *", false)))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "create",
		"--title", "New Repo", "--description", "d", "--product", "feedless", "--cron", "0 0 * * *", "--visibility", "private")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "New Repo" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "New Repo")
	}
	if gotBody["product"] != "feedless" {
		t.Errorf("body[product] = %v, want feedless", gotBody["product"])
	}
	if gotBody["description"] != "d" {
		t.Errorf("body[description] = %v, want d", gotBody["description"])
	}
	if gotBody["refreshCron"] != "0 0 * * *" {
		t.Errorf("body[refreshCron] = %v, want the --cron value", gotBody["refreshCron"])
	}
	if gotBody["visibility"] != "private" {
		t.Errorf("body[visibility] = %v, want private", gotBody["visibility"])
	}
	if !strings.Contains(stdout.String(), "Title: New Repo") {
		t.Errorf("stdout = %q, want the created repository printed like `view`", stdout.String())
	}
}

func TestRepositoryCreate_FromInputStdin(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(repositoryJSON(testRepoID2, "From Stdin", "feedless", "public", "", false)))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	input := `{"title":"From Stdin","description":"d","product":"feedless","sources":[]}`

	_, stderr, err := runCmdWithStdin(input, "repo", "create", "--input", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "From Stdin" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "From Stdin")
	}
}

func TestRepositoryCreate_FlagsAndInput_MutuallyExclusive(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "create", "--title", "x", "--input", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error")
	}
	if !strings.Contains(err.Error(), "mutually exclusive") {
		t.Errorf("error = %v, want it to mention mutually exclusive", err)
	}
}

func TestRepositoryCreate_MissingTitle_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "create", "--product", "feedless")
	if err == nil {
		t.Fatal("Execute() error = nil, want --title required")
	}
	if !strings.Contains(err.Error(), "--title") {
		t.Errorf("error = %v, want it to mention --title", err)
	}
}

func TestRepositoryCreate_MissingProduct_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "create", "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want --product required")
	}
	if !strings.Contains(err.Error(), "--product") {
		t.Errorf("error = %v, want it to mention --product", err)
	}
}

func TestRepositoryUpdate_FieldFlags_NoIfMatch(t *testing.T) {
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
		_, _ = w.Write([]byte(repositoryJSON(testRepoID2, "Updated Title", "feedless", "public", "", false)))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "update", testRepoID2, "--title", "Updated Title", "--visibility", "public")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotIfMatch != "" {
		t.Errorf("If-Match = %q, want empty (no conditional update from field flags)", gotIfMatch)
	}
	if gotBody["title"] != "Updated Title" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "Updated Title")
	}
	if gotBody["visibility"] != "public" {
		t.Errorf("body[visibility] = %v, want public", gotBody["visibility"])
	}
	if _, hasDescription := gotBody["description"]; hasDescription {
		t.Errorf("body = %v, want no description key (no --description given)", gotBody)
	}
	if !strings.Contains(stdout.String(), "Title: Updated Title") {
		t.Errorf("stdout = %q, want the updated repository printed like `view`", stdout.String())
	}
}

func TestRepositoryUpdate_NothingToUpdate_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "update", testRepoID2)
	if err == nil {
		t.Fatal("Execute() error = nil, want a nothing-to-update error")
	}
	if !strings.Contains(err.Error(), "nothing to update") {
		t.Errorf("error = %v, want it to mention nothing to update", err)
	}
}

func TestRepositoryUpdate_400_Fails(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		_, _ = w.Write([]byte(`{"code":"BAD_REQUEST","message":"invalid title"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("repo", "update", testRepoID2, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want the server's 400 surfaced")
	}
	if !strings.Contains(err.Error(), "invalid title") {
		t.Errorf("error = %v, want the server's message", err)
	}
}

func TestRepositoryUpdate_InvalidID_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("repo", "update", "not-a-uuid", "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid-id error")
	}
}

// The TTY paths are tested directly against deleteRepository: cobra's test stdin is never a real *os.File.

func TestRepositoryDelete_Yes_DeletesWithoutPrompt_NoGET(t *testing.T) {
	var sawGet, sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			sawGet = true
			w.WriteHeader(http.StatusOK)
		case http.MethodDelete:
			sawDelete = true
			if r.URL.Path != "/api/v1/repositories/"+testRepoID2 {
				t.Errorf("DELETE path = %q, want the repository's path", r.URL.Path)
			}
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("repo", "delete", testRepoID2, "--yes")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}
	if sawGet {
		t.Error("GET called despite --yes, want the title-fetching GET skipped")
	}
	if !sawDelete {
		t.Error("DELETE was never called")
	}
	if stdout.String() != "deleted "+testRepoID2+"\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "deleted "+testRepoID2+"\n")
	}
}

func TestRepositoryDelete_NotTTY_NoYes_Refused(t *testing.T) {
	called := false

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("repo", "delete", testRepoID2)
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

func TestDeleteRepository_TTY_Accepted_FetchesTitleAndDeletes(t *testing.T) {
	var sawGet, sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			sawGet = true
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(repositoryJSON(testRepoID2, testRepoName, "feedless", "public", "", false)))
		case http.MethodDelete:
			sawDelete = true
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, stderr := newConfirmTestCmd("y\n")

	err := deleteRepository(cmd, apiClient, mustUUID(t, testRepoID2), true, false)
	if err != nil {
		t.Fatalf("deleteRepository() error = %v, stderr = %q", err, stderr.String())
	}
	if !sawGet {
		t.Error("GET was never called to fetch the title for the prompt")
	}
	if !sawDelete {
		t.Error("DELETE was never called")
	}
	if !strings.Contains(stderr.String(), "Delete repository "+testRepoName+" ("+testRepoID2+")? [y/N] ") {
		t.Errorf("stderr = %q, want the exact confirmation prompt naming the fetched title", stderr.String())
	}
	if stdout.String() != "deleted "+testRepoID2+"\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "deleted "+testRepoID2+"\n")
	}
}

func TestDeleteRepository_TTY_Declined_ExitCode2_NoDelete(t *testing.T) {
	var sawDelete bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(repositoryJSON(testRepoID2, testRepoName, "feedless", "public", "", false)))
		case http.MethodDelete:
			sawDelete = true
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newConfirmTestCmd("n\n")

	err := deleteRepository(cmd, apiClient, mustUUID(t, testRepoID2), true, false)

	var declined *deleteDeclinedError
	if !errors.As(err, &declined) {
		t.Fatalf("deleteRepository() error = %v, want *deleteDeclinedError", err)
	}
	if declined.ExitCode() != ExitCancelled {
		t.Errorf("ExitCode() = %d, want %d", declined.ExitCode(), ExitCancelled)
	}
	if sawDelete {
		t.Error("DELETE was called despite the prompt being declined")
	}
}
