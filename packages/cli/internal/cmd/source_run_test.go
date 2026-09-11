package cmd

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"sync/atomic"
	"testing"
	"time"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

const testHarvestID = "44444444-4444-4444-4444-444444444444"

// ok and itemsAdded are only non-null together, as a real server sends them.
func harvestJSON(id, sourceID, status string, dryRun bool, ok *string, itemsAdded int, startedAt, finishedAt string) string {
	okField := "null"
	itemsAddedField := "null"

	if ok != nil {
		okField = *ok
		itemsAddedField = strconv.Itoa(itemsAdded)
	}

	finished := "null"
	if finishedAt != "" {
		finished = `"` + finishedAt + `"`
	}

	return `{
		"id": "` + id + `",
		"sourceId": "` + sourceID + `",
		"status": "` + status + `",
		"dryRun": ` + strconv.FormatBool(dryRun) + `,
		"ok": ` + okField + `,
		"itemsAdded": ` + itemsAddedField + `,
		"itemsIgnored": 0,
		"startedAt": "` + startedAt + `",
		"finishedAt": ` + finished + `
	}`
}

func TestSourceRun_FlowWithoutDryRun_LocalErrorBeforeAnyRequest(t *testing.T) {
	called := false
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("source", "run", testSourceID, "-R", testRepoID, "--flow", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error for --flow without --dry-run")
	}
	if !strings.Contains(err.Error(), "--dry-run") {
		t.Errorf("error = %v, want it to mention --dry-run", err)
	}
	if called {
		t.Error("server was called, want --flow/--dry-run validated locally first")
	}
}

func TestSourceRun_NoWait_PrintsIDAndExitsZero(t *testing.T) {
	var gotPath, gotMethod string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotMethod = r.Method
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Location", r.URL.Path+"/"+testHarvestID)
		w.WriteHeader(http.StatusAccepted)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", false, nil, 0, "2026-01-01T00:00:00Z", "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "run", testSourceID, "-R", testRepoID, "--no-wait")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotMethod != http.MethodPost || gotPath != "/api/v1/repositories/"+testRepoID+"/sources/"+testSourceID+"/harvests" {
		t.Errorf("request = %s %s, want a POST to the harvests endpoint", gotMethod, gotPath)
	}
	if got := strings.TrimSpace(stdout.String()); got != testHarvestID {
		t.Errorf("stdout = %q, want just the harvest id %q", got, testHarvestID)
	}
}

func TestSourceRun_NoWait_JSON_PrintsQueuedHarvest(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusAccepted)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", true, nil, 0, "2026-01-01T00:00:00Z", "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("source", "run", testSourceID, "-R", testRepoID, "--dry-run", "--no-wait", "--json=id,status,dryRun")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var row map[string]any
	if jsonErr := json.Unmarshal(stdout.Bytes(), &row); jsonErr != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", jsonErr, stdout.String())
	}
	if row["id"] != testHarvestID || row["status"] != "queued" || row["dryRun"] != true {
		t.Errorf("row = %v, unexpected", row)
	}
}

func TestSourceRun_DryRunWithFlow_SendsDryRunAndFlowInBody(t *testing.T) {
	dir := t.TempDir()
	path := dir + "/flow.json"
	writeFile(t, path, validFlowJSON)

	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusAccepted)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", true, nil, 0, "2026-01-01T00:00:00Z", "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("source", "run", testSourceID, "-R", testRepoID, "--dry-run", "--flow", path, "--no-wait")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotBody["dryRun"] != true {
		t.Errorf("body[dryRun] = %v, want true", gotBody["dryRun"])
	}
	if _, ok := gotBody["flow"]; !ok {
		t.Errorf("body = %v, want a flow key from --flow", gotBody)
	}
}

func TestSourceRun_RealRun_NoDryRunNoFlowInBody(t *testing.T) {
	var gotBody map[string]any

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotBody)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusAccepted)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", false, nil, 0, "2026-01-01T00:00:00Z", "")))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("source", "run", testSourceID, "-R", testRepoID, "--no-wait")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if _, ok := gotBody["dryRun"]; ok {
		t.Errorf("body = %v, want no dryRun key for a real run", gotBody)
	}
	if _, ok := gotBody["flow"]; ok {
		t.Errorf("body = %v, want no flow key (no --flow given)", gotBody)
	}
}

// These call runSourceRun directly with a fake sleeper and clock, so the wait loop never really sleeps.

func runSourceRunViaSeam(
	t *testing.T, srvURL string, f sourceRunFlags, flow *api.ScrapeFlow, deps pollDeps,
) (stdout, stderr string, err error) {
	t.Helper()

	cmd, stdoutBuf, stderrBuf := newEditorTestCmd()
	apiClient := newEditorTestClient(t, srvURL)
	repoID := mustUUID(t, testRepoID)
	sourceID := mustUUID(t, testSourceID)

	runErr := runSourceRun(cmd, apiClient, repoID, sourceID, f, flow, output.JSONFlags{}, deps)

	return stdoutBuf.String(), stderrBuf.String(), runErr
}

// harvestSequenceServer repeats the last status once exhausted; getCalls counts the polls.
func harvestSequenceServer(t *testing.T, statuses []string, ok *string, itemsAdded int, dryRun bool) (srv *httptest.Server, getCalls *int32) {
	t.Helper()

	var calls int32

	srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusAccepted)
			_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", dryRun, nil, 0, "2026-01-01T00:00:00Z", "")))

			return
		}

		if strings.HasSuffix(r.URL.Path, "/logs") {
			w.Header().Set("Content-Type", "text/plain")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte("dry run extracted no items\n"))

			return
		}

		n := int(atomic.AddInt32(&calls, 1)) - 1
		if n >= len(statuses) {
			n = len(statuses) - 1
		}

		status := statuses[n]

		var thisOK *string

		finished := ""
		if status == "completed" {
			thisOK = ok
			finished = "2026-01-01T00:00:12Z"
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, status, dryRun, thisOK, itemsAdded, "2026-01-01T00:00:00Z", finished)))
	}))
	t.Cleanup(srv.Close)

	return srv, &calls
}

func TestSourceRun_Waits_SucceedsOnCompletedOK_NoLogPrinted(t *testing.T) {
	okTrue := "true"
	srv, calls := harvestSequenceServer(t, []string{"queued", "running", "completed"}, &okTrue, 3, false)

	stdout, _, err := runSourceRunViaSeam(t, srv.URL, sourceRunFlags{}, nil, pollDeps{sleep: alwaysSleepsOK, now: time.Now})
	if err != nil {
		t.Fatalf("runSourceRun() error = %v", err)
	}

	if got := atomic.LoadInt32(calls); got != 3 {
		t.Errorf("GET .../harvests/{id} called %d times, want 3 (queued, running, completed)", got)
	}

	if !strings.Contains(stdout, "Result: succeeded") {
		t.Errorf("stdout = %q, want a succeeded summary", stdout)
	}
	if !strings.Contains(stdout, "Items: 3") {
		t.Errorf("stdout = %q, want Items: 3", stdout)
	}
	if !strings.Contains(stdout, "Dry run: no") {
		t.Errorf("stdout = %q, want Dry run: no", stdout)
	}
	if strings.Contains(stdout, "dry run extracted no items") {
		t.Errorf("stdout = %q, want no log printed on a successful real run", stdout)
	}
}

func TestSourceRun_Waits_FailsOnCompletedNotOK_PrintsLog_ExitCode1(t *testing.T) {
	okFalse := "false"
	srv, _ := harvestSequenceServer(t, []string{"completed"}, &okFalse, 0, true)

	stdout, stderr, err := runSourceRunViaSeam(t, srv.URL, sourceRunFlags{dryRun: true}, nil, pollDeps{sleep: alwaysSleepsOK, now: time.Now})

	assertExitCode(t, err, 1)

	if !strings.Contains(stdout, "Result: failed") {
		t.Errorf("stdout = %q, want a failed summary", stdout)
	}
	if !strings.Contains(stdout, "dry run extracted no items") {
		t.Errorf("stdout = %q, want the log printed for a failed dry run", stdout)
	}
	if stderr != "" {
		t.Errorf("stderr = %q, want empty (harvestNotOKError renders nothing further)", stderr)
	}
}

func TestSourceRun_Waits_FailsOnCompletedNotOK_RealRun_AlsoPrintsLog(t *testing.T) {
	okFalse := "false"
	srv, _ := harvestSequenceServer(t, []string{"completed"}, &okFalse, 0, false)

	stdout, _, err := runSourceRunViaSeam(t, srv.URL, sourceRunFlags{}, nil, pollDeps{sleep: alwaysSleepsOK, now: time.Now})

	assertExitCode(t, err, 1)

	if !strings.Contains(stdout, "Result: failed") {
		t.Errorf("stdout = %q, want a failed summary", stdout)
	}
	if !strings.Contains(stdout, "dry run extracted no items") {
		t.Errorf("stdout = %q, want the log printed for a failed real run too (not just dry runs)", stdout)
	}
}

func TestSourceRun_Waits_SuccessfulDryRun_AlsoPrintsLog(t *testing.T) {
	okTrue := "true"
	srv, _ := harvestSequenceServer(t, []string{"completed"}, &okTrue, 2, true)

	stdout, _, err := runSourceRunViaSeam(t, srv.URL, sourceRunFlags{dryRun: true}, nil, pollDeps{sleep: alwaysSleepsOK, now: time.Now})
	if err != nil {
		t.Fatalf("runSourceRun() error = %v", err)
	}

	if !strings.Contains(stdout, "Result: succeeded") {
		t.Errorf("stdout = %q, want a succeeded summary", stdout)
	}
	if !strings.Contains(stdout, "dry run extracted no items") {
		t.Errorf("stdout = %q, want the log printed for a successful dry run too (it lists the extracted items)", stdout)
	}
}

func TestSourceRun_Waits_JSON_PrintsFinalHarvest_NoLog(t *testing.T) {
	okFalse := "false"
	srv, _ := harvestSequenceServer(t, []string{"completed"}, &okFalse, 0, true)

	cmd, stdoutBuf, _ := newEditorTestCmd()
	apiClient := newEditorTestClient(t, srv.URL)
	repoID := mustUUID(t, testRepoID)
	sourceID := mustUUID(t, testSourceID)

	jf := output.JSONFlags{Requested: true, Fields: []string{"id", "status", "ok"}}
	deps := pollDeps{sleep: alwaysSleepsOK, now: time.Now}

	err := runSourceRun(cmd, apiClient, repoID, sourceID, sourceRunFlags{dryRun: true}, nil, jf, deps)
	assertExitCode(t, err, 1)

	var row map[string]any
	if jsonErr := json.Unmarshal(stdoutBuf.Bytes(), &row); jsonErr != nil {
		t.Fatalf("stdout isn't valid JSON: %v (%s)", jsonErr, stdoutBuf.String())
	}
	if row["id"] != testHarvestID || row["status"] != "completed" || row["ok"] != false {
		t.Errorf("row = %v, unexpected", row)
	}
	if strings.Contains(stdoutBuf.String(), "dry run extracted no items") {
		t.Errorf("stdout = %q, want no log mixed into --json output", stdoutBuf.String())
	}
}

// The fake sleeper cancels the context, standing in for SIGINT.
func TestSourceRun_Interrupted_StopsPolling_PrintsHint_ReturnsCancelled(t *testing.T) {
	var getCalled bool

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		if r.Method == http.MethodPost {
			w.WriteHeader(http.StatusAccepted)
			_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", false, nil, 0, "2026-01-01T00:00:00Z", "")))

			return
		}

		getCalled = true
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(harvestJSON(testHarvestID, testSourceID, "queued", false, nil, 0, "2026-01-01T00:00:00Z", "")))
	}))
	t.Cleanup(srv.Close)

	cmd, _, stderrBuf := newEditorTestCmd()

	ctx, cancel := context.WithCancel(context.Background())
	cmd.SetContext(ctx)

	apiClient := newEditorTestClient(t, srv.URL)
	repoID := mustUUID(t, testRepoID)
	sourceID := mustUUID(t, testSourceID)

	deps := pollDeps{
		sleep: func(_ context.Context, _ time.Duration) error {
			cancel() // the "Ctrl-C" for this test

			return context.Canceled
		},
		now: time.Now,
	}

	err := runSourceRun(cmd, apiClient, repoID, sourceID, sourceRunFlags{}, nil, output.JSONFlags{}, deps)
	if !errors.Is(err, context.Canceled) {
		t.Fatalf("runSourceRun() error = %v, want context.Canceled", err)
	}

	if getCalled {
		t.Error("GET .../harvests/{id} was called, want polling to stop before ever fetching")
	}

	want := "harvest " + testHarvestID + " keeps running on the server — see: feedctl harvest view " + testHarvestID +
		" -R " + testRepoID + " -S " + testSourceID
	if !strings.Contains(stderrBuf.String(), want) {
		t.Errorf("stderr = %q, want it to contain %q", stderrBuf.String(), want)
	}
}

func TestSourceRun_FullCommandTree_MissingSource_MissingRepo(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("source", "run", testSourceID)
	if err == nil {
		t.Fatal("Execute() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}
