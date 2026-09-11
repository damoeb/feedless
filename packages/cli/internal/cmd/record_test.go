package cmd

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

const (
	testRecordID  = "77777777-7777-7777-7777-777777777777"
	testRecordID2 = "88888888-8888-8888-8888-888888888888"
	testRecordID3 = "99999999-9999-9999-9999-999999999999"
)

// recordJSON builds a minimal Record JSON body covering every required
// field of the schema (id, url, createdAt, publishedAt, updatedAt) plus the
// optional ones these tests check (title, text, tags, imageUrl).
func recordJSON(id, url, title string) string {
	return `{
		"id": "` + id + `",
		"url": "` + url + `",
		"title": "` + title + `",
		"text": "body text",
		"tags": ["a", "b"],
		"imageUrl": "https://example.com/img.png",
		"createdAt": "2024-01-01T00:00:00Z",
		"publishedAt": "2024-01-02T00:00:00Z",
		"updatedAt": "2024-01-03T00:00:00Z"
	}`
}

// --- record list ---

func TestRecordList_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "list")
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want the exact brief message", err.Error())
	}
}

func TestRecordList_RendersTable(t *testing.T) {
	var gotPath string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` + recordJSON(testRecordID, "https://example.com/a", "A Title") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "list", "-R", testRepoID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/repositories/"+testRepoID+"/records" {
		t.Errorf("path = %q, want the repository's records endpoint", gotPath)
	}

	fields := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\t")
	if len(fields) != 4 {
		t.Fatalf("row fields = %v (len %d), want 4 (ID TITLE URL PUBLISHED)", fields, len(fields))
	}
	if fields[0] != testRecordID || fields[1] != "A Title" || fields[2] != "https://example.com/a" {
		t.Errorf("row = %v, unexpected", fields)
	}
	if fields[3] != "2024-01-02T00:00:00Z" {
		t.Errorf("PUBLISHED column = %q, want RFC 3339 when piped", fields[3])
	}
}

func TestRecordList_JSON_Fields_ExcludesTags(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` + recordJSON(testRecordID, "https://example.com/a", "A Title") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "list", "-R", testRepoID, "--json=id,url,title")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var rows []map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &rows); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if len(rows) != 1 || rows[0]["id"] != testRecordID || rows[0]["url"] != "https://example.com/a" || rows[0]["title"] != "A Title" {
		t.Errorf("rows = %v, unexpected", rows)
	}
}

func TestRecordList_BareJSON_ListsFields_ExcludesTags(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "list", "-R", testRepoID, "--json")
	if err == nil {
		t.Fatal("Execute() error = nil, want a FieldsError for bare --json")
	}
	if !strings.Contains(err.Error(), "title") {
		t.Errorf("error = %v, want it to list the field names", err)
	}
	if strings.Contains(err.Error(), "  tags") {
		t.Errorf("error = %v, want list's field names to exclude tags (reserved for view)", err)
	}
}

func TestRecordList_Limit_Paginates(t *testing.T) {
	var pages int

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		pages++
		page := r.URL.Query().Get("page")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)

		if page == "0" {
			_, _ = w.Write([]byte(`{"items":[` + recordJSON(testRecordID, "https://example.com/1", "R1") + `],"hasMore":true}`))

			return
		}

		_, _ = w.Write([]byte(`{"items":[` + recordJSON(testRecordID2, "https://example.com/2", "R2") + `],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "list", "-R", testRepoID, "--limit", "2")
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

// --- record view ---

func TestRecordView_RendersFields(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet || r.URL.Path != "/api/v1/repositories/"+testRepoID+"/records/"+testRecordID {
			t.Errorf("request = %s %s, want GET of the record", r.Method, r.URL.Path)
		}
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("ETag", `"abc123"`)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "A Title")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "view", testRecordID, "-R", testRepoID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	for _, want := range []string{
		"Title: A Title",
		"URL: https://example.com/a",
		"Published: 2024-01-02T00:00:00Z",
		"Created: 2024-01-01T00:00:00Z",
		"Updated: 2024-01-03T00:00:00Z",
		"Tags: a, b",
		"Image URL: https://example.com/img.png",
		"Text:",
		"body text",
	} {
		if !strings.Contains(out, want) {
			t.Errorf("stdout = %q, want it to contain %q", out, want)
		}
	}
}

func TestRecordView_JSON_PrintsRecord_IncludingTags(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "A Title")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "view", testRecordID, "-R", testRepoID, "--json=id,title,tags")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var row map[string]any
	if err := json.Unmarshal(stdout.Bytes(), &row); err != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", err, stdout.String())
	}
	if row["id"] != testRecordID {
		t.Errorf("row[id] = %v, want %q", row["id"], testRecordID)
	}
	tags, ok := row["tags"].([]any)
	if !ok || len(tags) != 2 {
		t.Errorf("row[tags] = %v, want the record's tags (proving view's --json includes tags)", row["tags"])
	}
}

func TestRecordView_404(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("record", "view", testRecordID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a 404 error")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error = %v, want it to say not found", err)
	}
}

// --- record create ---

func TestRecordCreate_FromFlags(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost || r.URL.Path != "/api/v1/repositories/"+testRepoID+"/records" {
			t.Errorf("request = %s %s, want POST of the repository's records endpoint", r.Method, r.URL.Path)
		}
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/new", "New Record")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "create", "-R", testRepoID,
		"--title", "New Record", "--url", "https://example.com/new", "--text", "hello",
		"--tags", "a,b", "--published", "2024-01-02T00:00:00Z")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "New Record" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "New Record")
	}
	if gotBody["url"] != "https://example.com/new" {
		t.Errorf("body[url] = %v, want the --url value", gotBody["url"])
	}
	if gotBody["text"] != "hello" {
		t.Errorf("body[text] = %v, want %q", gotBody["text"], "hello")
	}
	if gotBody["publishedAt"] != "2024-01-02T00:00:00Z" {
		t.Errorf("body[publishedAt] = %v, want the --published value", gotBody["publishedAt"])
	}
	tags, ok := gotBody["tags"].([]any)
	if !ok || len(tags) != 2 || tags[0] != "a" || tags[1] != "b" {
		t.Errorf("body[tags] = %v, want [a b]", gotBody["tags"])
	}
	if !strings.Contains(stdout.String(), "Title: New Record") {
		t.Errorf("stdout = %q, want the created record printed like `view`", stdout.String())
	}
}

func TestRecordCreate_FromInputStdin(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/stdin", "From Stdin")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	input := `{"title":"From Stdin","url":"https://example.com/stdin","publishedAt":"2024-01-02T00:00:00Z"}`

	_, stderr, err := runCmdWithStdin(input, "record", "create", "-R", testRepoID, "--input", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["title"] != "From Stdin" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "From Stdin")
	}
}

func TestRecordCreate_FlagsAndInput_MutuallyExclusive(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "create", "-R", testRepoID, "--title", "x", "--input", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error")
	}
	if !strings.Contains(err.Error(), "mutually exclusive") {
		t.Errorf("error = %v, want it to mention mutually exclusive", err)
	}
}

func TestRecordCreate_MissingTitle_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "create", "-R", testRepoID, "--url", "https://example.com", "--published", "2024-01-02T00:00:00Z")
	if err == nil {
		t.Fatal("Execute() error = nil, want --title required")
	}
	if !strings.Contains(err.Error(), "--title") {
		t.Errorf("error = %v, want it to mention --title", err)
	}
}

func TestRecordCreate_MissingURL_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "create", "-R", testRepoID, "--title", "x", "--published", "2024-01-02T00:00:00Z")
	if err == nil {
		t.Fatal("Execute() error = nil, want --url required")
	}
	if !strings.Contains(err.Error(), "--url") {
		t.Errorf("error = %v, want it to mention --url", err)
	}
}

func TestRecordCreate_MissingPublished_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "create", "-R", testRepoID, "--title", "x", "--url", "https://example.com")
	if err == nil {
		t.Fatal("Execute() error = nil, want --published required")
	}
	if !strings.Contains(err.Error(), "--published") {
		t.Errorf("error = %v, want it to mention --published", err)
	}
}

func TestRecordCreate_InvalidPublished_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "create", "-R", testRepoID, "--title", "x", "--url", "https://example.com", "--published", "not-a-date")
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid --published error")
	}
	if !strings.Contains(err.Error(), "--published") {
		t.Errorf("error = %v, want it to mention --published", err)
	}
}

// --- record update (field flags, non-editor) ---

func TestRecordUpdate_FieldFlags_NoIfMatch(t *testing.T) {
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
		_, _ = w.Write([]byte(recordJSON(testRecordID, "https://example.com/a", "Updated Title")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "update", testRecordID, "-R", testRepoID, "--title", "Updated Title")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotIfMatch != "" {
		t.Errorf("If-Match = %q, want empty (no conditional update from field flags)", gotIfMatch)
	}
	if gotBody["title"] != "Updated Title" {
		t.Errorf("body[title] = %v, want %q", gotBody["title"], "Updated Title")
	}
	if _, hasURL := gotBody["url"]; hasURL {
		t.Errorf("body = %v, want no url key (no --url given)", gotBody)
	}
	if _, hasText := gotBody["text"]; hasText {
		t.Errorf("body = %v, want no text key (no --text given)", gotBody)
	}
	if _, hasTags := gotBody["tags"]; hasTags {
		t.Errorf("body = %v, want no tags key (no --tags given)", gotBody)
	}
	if !strings.Contains(stdout.String(), "Title: Updated Title") {
		t.Errorf("stdout = %q, want the updated record printed like `view`", stdout.String())
	}
}

func TestRecordUpdate_NothingToUpdate_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "update", testRecordID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a nothing-to-update error")
	}
	if !strings.Contains(err.Error(), "nothing to update") {
		t.Errorf("error = %v, want it to mention nothing to update", err)
	}
}

func TestRecordUpdate_400_Fails(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		_, _ = w.Write([]byte(`{"code":"BAD_REQUEST","message":"invalid url"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("record", "update", testRecordID, "-R", testRepoID, "--url", "not a url")
	if err == nil {
		t.Fatal("Execute() error = nil, want the server's 400 surfaced")
	}
	if !strings.Contains(err.Error(), "invalid url") {
		t.Errorf("error = %v, want the server's message", err)
	}
}

func TestRecordUpdate_InvalidID_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "update", "not-a-uuid", "-R", testRepoID, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid-id error")
	}
}

func TestRecordUpdate_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "update", testRecordID, "--title", "x")
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want the exact brief message", err.Error())
	}
}

// --- record delete ---

func TestRecordDelete_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("record", "delete", testRecordID, "--yes")
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want the exact brief message", err.Error())
	}
}

func TestRecordDelete_InvalidUUID_RejectedBeforeAnyRequest(t *testing.T) {
	called := false

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("record", "delete", testRecordID, "not-a-uuid", testRecordID2, "-R", testRepoID, "--yes")
	if err == nil {
		t.Fatal("Execute() error = nil, want an invalid-id error")
	}
	if called {
		t.Error("server was called, want every id validated before any request")
	}
}

func TestRecordDelete_ThreeIds_SecondFails_TwoDeletedOneErrorLine_Exit1(t *testing.T) {
	var deleteCalls []string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodDelete {
			t.Errorf("method = %s, want DELETE", r.Method)
		}

		id := strings.TrimPrefix(r.URL.Path, "/api/v1/repositories/"+testRepoID+"/records/")
		deleteCalls = append(deleteCalls, id)

		switch id {
		case testRecordID2:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
		default:
			w.WriteHeader(http.StatusNoContent)
		}
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("record", "delete", testRecordID, testRecordID2, testRecordID3, "-R", testRepoID, "--yes")

	assertExitCode(t, err, 1)

	if len(deleteCalls) != 3 || deleteCalls[0] != testRecordID || deleteCalls[1] != testRecordID2 || deleteCalls[2] != testRecordID3 {
		t.Errorf("DELETE calls = %v, want all three ids in order", deleteCalls)
	}

	// stdout carries only the successful deletes, in order — no error text
	// mixed in, so a script capturing stdout alone learns exactly what was
	// deleted.
	outLines := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\n")
	if len(outLines) != 2 {
		t.Fatalf("stdout lines = %v, want 2 (the two successful deletes)", outLines)
	}
	if outLines[0] != "deleted "+testRecordID {
		t.Errorf("stdout line 0 = %q, want %q", outLines[0], "deleted "+testRecordID)
	}
	if outLines[1] != "deleted "+testRecordID3 {
		t.Errorf("stdout line 1 = %q, want %q", outLines[1], "deleted "+testRecordID3)
	}
	if strings.Contains(stdout.String(), "error:") {
		t.Errorf("stdout = %q, want no error text on stdout", stdout.String())
	}

	// stderr carries only the failed id's error line.
	errLines := strings.Split(strings.TrimRight(stderr.String(), "\n"), "\n")
	if len(errLines) != 1 {
		t.Fatalf("stderr lines = %v, want 1 (the one failed delete)", errLines)
	}
	if !strings.HasPrefix(errLines[0], "error: "+testRecordID2+": ") {
		t.Errorf("stderr line = %q, want it to start with %q", errLines[0], "error: "+testRecordID2+": ")
	}
	if strings.Contains(stderr.String(), "deleted "+testRecordID) || strings.Contains(stderr.String(), "deleted "+testRecordID3) {
		t.Errorf("stderr = %q, want no successful-delete lines on stderr", stderr.String())
	}
}

func TestRecordDelete_NotTTY_NoYes_Refused_BeforeAnyRequest(t *testing.T) {
	called := false

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("record", "delete", testRecordID, testRecordID2, "-R", testRepoID)
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

func TestDeleteRecords_TTY_ConfirmsOncePerBatch_ThenDeletesEach(t *testing.T) {
	var deleteCalls int

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		deleteCalls++
		w.WriteHeader(http.StatusNoContent)
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, stdout, stderr := newConfirmTestCmd("y\n")

	ids := []api.RecordId{mustUUID(t, testRecordID), mustUUID(t, testRecordID2)}

	err := deleteRecords(cmd, apiClient, mustUUID(t, testRepoID), ids, true, false)
	if err != nil {
		t.Fatalf("deleteRecords() error = %v, stderr = %q", err, stderr.String())
	}

	if deleteCalls != 2 {
		t.Errorf("DELETE calls = %d, want 2", deleteCalls)
	}

	prompt := "Delete 2 records from " + testRepoID + "? [y/N] "
	if got := strings.Count(stderr.String(), prompt); got != 1 {
		t.Errorf("prompt printed %d times, want exactly 1 (once per batch), stderr = %q", got, stderr.String())
	}

	wantOut := "deleted " + testRecordID + "\ndeleted " + testRecordID2 + "\n"
	if stdout.String() != wantOut {
		t.Errorf("stdout = %q, want %q", stdout.String(), wantOut)
	}
}

func TestDeleteRecords_TTY_Declined_ExitCode2_NoDeletesSent(t *testing.T) {
	var deleteCalls int

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		deleteCalls++
		w.WriteHeader(http.StatusNoContent)
	}))
	t.Cleanup(srv.Close)

	apiClient := newEditorTestClient(t, srv.URL)
	cmd, _, _ := newConfirmTestCmd("n\n")

	ids := []api.RecordId{mustUUID(t, testRecordID), mustUUID(t, testRecordID2)}

	err := deleteRecords(cmd, apiClient, mustUUID(t, testRepoID), ids, true, false)

	assertExitCode(t, err, ExitCancelled)

	if deleteCalls != 0 {
		t.Errorf("DELETE calls = %d, want 0 (declined before any request)", deleteCalls)
	}
}
