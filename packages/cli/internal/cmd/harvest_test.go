package cmd

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"
)

// --- harvest list ---

func TestHarvestList_HitsHarvestsEndpoint_NoDryRunParamByDefault(t *testing.T) {
	var gotPath string
	var gotQuery url.Values

	okTrue := "true"

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` +
			harvestJSON(testHarvestID, testSourceID, "completed", false, &okTrue, 5, "2026-01-01T00:00:00Z", "2026-01-01T00:00:10Z") +
			`],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "list", "-R", testRepoID, "-S", testSourceID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	wantPath := "/api/v1/repositories/" + testRepoID + "/sources/" + testSourceID + "/harvests"
	if gotPath != wantPath {
		t.Errorf("path = %q, want %q", gotPath, wantPath)
	}
	if gotQuery.Has("dryRun") {
		t.Errorf("query = %v, want no dryRun param without --dry-run", gotQuery)
	}

	fields := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\t")
	if len(fields) != 6 {
		t.Fatalf("row fields = %v (len %d), want 6 (ID STATUS RESULT ITEMS STARTED DURATION)", fields, len(fields))
	}
	if fields[0] != testHarvestID || fields[1] != "completed" || fields[2] != "ok" || fields[3] != "5" {
		t.Errorf("row = %v, unexpected", fields)
	}
	if fields[5] != "10s" {
		t.Errorf("DURATION column = %q, want 10s", fields[5])
	}
}

func TestHarvestList_DryRunFlag_PassesDryRunTrue(t *testing.T) {
	var gotQuery url.Values

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("harvest", "list", "-R", testRepoID, "-S", testSourceID, "--dry-run")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotQuery.Get("dryRun") != "true" {
		t.Errorf("dryRun query param = %q, want true", gotQuery.Get("dryRun"))
	}
}

func TestHarvestList_QueuedAndRunning_ResultColumnIsDash(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` +
			harvestJSON(testHarvestID, testSourceID, "running", false, nil, 0, "2026-01-01T00:00:00Z", "") +
			`],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "list", "-R", testRepoID, "-S", testSourceID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	fields := strings.Split(strings.TrimRight(stdout.String(), "\n"), "\t")
	if fields[2] != "-" {
		t.Errorf("RESULT column = %q, want - for a still-running harvest", fields[2])
	}
	if fields[3] != "-" {
		t.Errorf("ITEMS column = %q, want - (itemsAdded is only meaningful once completed)", fields[3])
	}
	if fields[5] != "-" {
		t.Errorf("DURATION column = %q, want - (not finished yet)", fields[5])
	}
}

func TestHarvestList_JSON_Fields(t *testing.T) {
	okFalse := "false"

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[` +
			harvestJSON(testHarvestID, testSourceID, "completed", true, &okFalse, 0, "2026-01-01T00:00:00Z", "2026-01-01T00:00:05Z") +
			`],"hasMore":false}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "list", "-R", testRepoID, "-S", testSourceID, "--json=id,status,ok,dryRun")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var rows []map[string]any
	if jsonErr := json.Unmarshal(stdout.Bytes(), &rows); jsonErr != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", jsonErr, stdout.String())
	}
	if len(rows) != 1 || rows[0]["id"] != testHarvestID || rows[0]["ok"] != false || rows[0]["dryRun"] != true {
		t.Errorf("rows = %v, unexpected", rows)
	}
}

func TestHarvestList_MissingSource_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("harvest", "list", "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want -S/--source is required")
	}
	if err.Error() != "-S/--source is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-S/--source is required")
	}
}

func TestHarvestList_MissingRepo_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("harvest", "list", "-S", testSourceID)
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}

// --- harvest view ---

func TestHarvestView_Summary(t *testing.T) {
	okTrue := "true"

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet || r.URL.Path != "/api/v1/repositories/"+testRepoID+"/sources/"+testSourceID+"/harvests/"+testHarvestID {
			t.Errorf("request = %s %s, want GET of the harvest", r.Method, r.URL.Path)
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "completed", true, &okTrue, 7, "2026-01-01T00:00:00Z", "2026-01-01T00:00:20Z")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID, "-S", testSourceID)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	for _, want := range []string{"Result: succeeded", "Dry run: yes", "Items: 7", "Duration: 20s"} {
		if !strings.Contains(out, want) {
			t.Errorf("stdout = %q, want it to contain %q", out, want)
		}
	}
}

func TestHarvestView_Log_PrintsOnlyLogText_WithTextPlainAccept(t *testing.T) {
	var gotAccept, gotPath string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotAccept = r.Header.Get("Accept")
		gotPath = r.URL.Path
		w.Header().Set("Content-Type", "text/plain")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("line one\nline two\n"))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID, "-S", testSourceID, "--log")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	wantPath := "/api/v1/repositories/" + testRepoID + "/sources/" + testSourceID + "/harvests/" + testHarvestID + "/logs"
	if gotPath != wantPath {
		t.Errorf("path = %q, want %q", gotPath, wantPath)
	}
	if gotAccept != "text/plain" {
		t.Errorf("Accept header = %q, want text/plain", gotAccept)
	}
	if stdout.String() != "line one\nline two\n" {
		t.Errorf("stdout = %q, want just the log text", stdout.String())
	}
}

// --- C9: control-sequence sanitization ---

// maliciousLog is a harvest log body with a CSI clear-screen (ESC [ 2 J)
// and an OSC 0 terminal-title write (ESC ] 0 ; ... BEL) mixed into
// otherwise ordinary log lines.
func maliciousLog() string {
	esc, bel := string(rune(0x1b)), string(rune(0x07))

	return "line one\n" + esc + "[2J" + esc + "]0;pwned" + bel + "line two\n"
}

func TestHarvestView_Log_Piped_KeepsRawControlSequences(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/plain")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(maliciousLog()))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	// runCmd's stdout is a *bytes.Buffer, never a terminal — this exercises
	// exactly the non-TTY branch `harvest view --log > file` relies on to
	// save the server's exact bytes.
	stdout, stderr, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID, "-S", testSourceID, "--log")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if stdout.String() != maliciousLog() {
		t.Errorf("stdout = %q, want the raw log body byte-for-byte (piped output must stay byte-exact)", stdout.String())
	}
}

func TestSanitizeHarvestLog_NonTTY_ReturnsRawBytes(t *testing.T) {
	in := []byte(maliciousLog())

	got := sanitizeHarvestLog(in, false)
	if string(got) != string(in) {
		t.Errorf("sanitizeHarvestLog(_, false) = %q, want the input unchanged", got)
	}
}

func TestSanitizeHarvestLog_TTY_StripsControlSequences(t *testing.T) {
	got := sanitizeHarvestLog([]byte(maliciousLog()), true)

	if strings.ContainsRune(string(got), 0x1b) {
		t.Errorf("sanitizeHarvestLog(_, true) = %q, want no ESC (0x1b)", got)
	}
	if strings.ContainsRune(string(got), 0x07) {
		t.Errorf("sanitizeHarvestLog(_, true) = %q, want no BEL (0x07)", got)
	}
	if !strings.Contains(string(got), "line one") || !strings.Contains(string(got), "line two") {
		t.Errorf("sanitizeHarvestLog(_, true) = %q, want the surrounding log text preserved", got)
	}
}

func TestHarvestView_JSON_PrintsHarvest(t *testing.T) {
	okFalse := "false"

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "completed", false, &okFalse, 0, "2026-01-01T00:00:00Z", "2026-01-01T00:00:01Z")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID, "-S", testSourceID, "--json=id,ok,status")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var row map[string]any
	if jsonErr := json.Unmarshal(stdout.Bytes(), &row); jsonErr != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", jsonErr, stdout.String())
	}
	if row["id"] != testHarvestID || row["ok"] != false || row["status"] != "completed" {
		t.Errorf("row = %v, unexpected", row)
	}
}

func TestHarvestView_404(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte(`{"code":"NOT_FOUND","message":"gone"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID, "-S", testSourceID)
	if err == nil {
		t.Fatal("Execute() error = nil, want a 404 error")
	}
	if !strings.Contains(err.Error(), "not found") {
		t.Errorf("error = %v, want it to say not found", err)
	}
}

func TestHarvestView_MissingSource_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("harvest", "view", testHarvestID, "-R", testRepoID)
	if err == nil {
		t.Fatal("Execute() error = nil, want -S/--source is required")
	}
	if err.Error() != "-S/--source is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-S/--source is required")
	}
}
