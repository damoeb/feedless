//go:build e2e

package e2e

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"testing"
	"time"
)

// scenarioTimeout bounds the whole scenario, stack startup included.
const scenarioTimeout = 10 * time.Minute

// fixtureItems are the item titles fixtures/site/items.html lists.
var fixtureItems = []string{"Alpha item", "Bravo item", "Charlie item"}

// statusPollTimeout bounds how long `feedctl status` may take to see the
// agent. StartStack already waited for the core to log it; this covers the
// registry write racing that log line.
const statusPollTimeout = 30 * time.Second

// waitForConnectedAgent polls `feedctl status --host <coreURL> --json`, with
// no login, until it exits 0 reporting at least one connected agent, then
// checks the human output once.
func waitForConnectedAgent(ctx context.Context, t *testing.T, cli *Feedctl, coreURL string) {
	t.Helper()

	type serverStatus struct {
		Agents struct {
			Connected int `json:"connected"`
		} `json:"agents"`
	}

	deadline := time.Now().Add(statusPollTimeout)

	for {
		res := cli.RunQuiet(ctx, "", "status", "--host", coreURL, "--json")
		if res.ExitCode == 0 && DecodeStdout[serverStatus](t, res).Agents.Connected >= 1 {
			break
		}

		if time.Now().After(deadline) {
			t.Fatalf("want feedctl status to report a connected agent within %s, last run:\n%s", statusPollTimeout, res)
		}

		select {
		case <-ctx.Done():
			t.Fatalf("waiting for feedctl status to report a connected agent: %v", ctx.Err())
		case <-time.After(2 * time.Second):
		}
	}

	status := cli.MustRun(ctx, 0, "", "status", "--host", coreURL)
	if !strings.Contains(status.Stdout, "Server:   "+coreURL+"\n") || !strings.Contains(status.Stdout, " connected\n") {
		t.Fatalf("want the status summary for %s, got:\n%s", coreURL, status)
	}
}

// TestBrokenSourceFixLoop runs the first feedctl use case end to end against
// a real core, agent and database: a source breaks, the user finds it,
// reads the failed harvest's log, dry-runs a fix without touching the saved
// source, saves the fix and runs it for real. It closes with the stale-edit
// guard (If-Match -> 412) the editor loop relies on.
func TestBrokenSourceFixLoop(t *testing.T) {
	ctx, cancel := context.WithTimeout(context.Background(), scenarioTimeout)
	defer cancel()

	SkipWithoutDocker(t)

	bin := BuildFeedctl(t)
	stack := StartStack(ctx, t)

	token, err := MintAPIToken(ctx, stack.CoreURL, stack.RootEmail, stack.RootSecretKey)
	if err != nil {
		t.Fatalf("minting an API token: %v", err)
	}

	cli := NewFeedctl(t, bin)

	t.Log("step 0: feedctl status answers without a login and reports the connected agent")

	waitForConnectedAgent(ctx, t, cli, stack.CoreURL)

	// A subtest, so a failure is reported by name without ending the
	// scenario, and a host without a shipped build skips only its binary.
	t.Log("step 0b: the core serves the feedctl downloads under /cli/** without a token")

	t.Run("cli downloads", func(t *testing.T) {
		checkCLIDownloads(ctx, t, stack.CoreURL, coreAPIGatewayURL)
	})

	login := cli.MustRun(ctx, 0, token, "auth", "login", "--url", stack.CoreURL, "--with-token")
	if !strings.Contains(login.Stderr, "plain text") {
		t.Fatalf("want the no-keyring file fallback warning, got:\n%s", login)
	}

	// Logged in, status resolves the default host on its own.
	if status := cli.MustRun(ctx, 0, "", "status"); !strings.Contains(status.Stdout, "Server:   "+stack.CoreURL+"\n") {
		t.Fatalf("want status to ask the logged-in default host, got:\n%s", status)
	}

	// hosts.yml holds the live token in plain text: check for it, never print it.
	hosts, err := os.ReadFile(cli.HostsFile())
	if err != nil {
		t.Fatalf("reading %s: %v", cli.HostsFile(), err)
	}

	if !strings.Contains(string(hosts), "token: "+token) {
		t.Fatalf("want the token stored in %s by the no-keyring file fallback, but it is not there (contents withheld: the file holds credentials)",
			cli.HostsFile())
	}

	brokenFlow := fixturePath(t, "flows", "broken.json")
	fixedFlow := fixturePath(t, "flows", "fixed.json")
	emptyFlow := fixturePath(t, "flows", "empty.json")

	// The broken flow's XPath matches nothing on the fixture page. Its fetch
	// is prerendered, so the agent runs the extract: it dereferences the
	// first match, fails, and reports the run as not ok — a real error that
	// counts towards errorsInSuccession, unlike the core's own "no items"
	// outcome, which the harvester treats as transient and resets to 0.
	t.Log("step 1: create a repository and a source whose flow extracts nothing")

	repoID := createRepository(ctx, t, cli)
	sourceID := createSource(ctx, t, cli, repoID, brokenFlow)

	// The scheduler harvests a new repository once on its own, seconds after
	// creation (see createRepository). Let that run finish first, so every
	// harvest from here on is one the scenario started.
	waitForHarvestsToSettle(ctx, t, cli, repoID, sourceID, true)

	t.Log("step 2: a real run fails, and the source shows up as errored across repositories")

	run := cli.MustRun(ctx, 1, "", "source", "run", sourceID, "-R", repoID)
	if !strings.Contains(run.Stdout, "Result: failed") {
		t.Fatalf("want a failed run summary, got:\n%s", run)
	}

	errored, found := erroredSources(ctx, t, cli)[sourceID]
	if !found || errored.ErrorsInSuccession < 1 || errored.RepositoryID != repoID {
		t.Fatalf("want source %s of repository %s listed by `source list --errored` with errorsInSuccession >= 1, got %+v (found %t)",
			sourceID, repoID, errored, found)
	}

	t.Log("step 3: the failed harvest's log shows the failure")

	harvests := DecodeStdout[[]harvestRow](t, cli.MustRun(ctx, 0, "",
		"harvest", "list", "-R", repoID, "-S", sourceID, "--json", "id,status,ok"))
	if len(harvests) == 0 || harvests[0].Status != "completed" || harvests[0].OK == nil || *harvests[0].OK {
		t.Fatalf("want the newest harvest completed and failed, got %+v", harvests)
	}

	harvestLog := cli.MustRun(ctx, 0, "", "harvest", "view", harvests[0].ID, "-R", repoID, "-S", sourceID, "--log")
	if !strings.Contains(harvestLog.Stdout, "scrape failed") {
		t.Fatalf("want the harvest log to show the scrape failure, got:\n%s", harvestLog)
	}

	t.Log("step 4: a dry run of the fixed flow extracts the fixture items and leaves the source alone")

	// Nothing may be harvesting the source while the snapshots are taken, or
	// its error state could change underneath the "unchanged" check.
	waitForHarvestsToSettle(ctx, t, cli, repoID, sourceID, false)

	before := viewSource(ctx, t, cli, repoID, sourceID)

	dryRun := cli.MustRun(ctx, 0, "", "source", "run", sourceID, "-R", repoID, "--dry-run", "--flow", fixedFlow)
	for _, item := range fixtureItems {
		if !strings.Contains(dryRun.Stdout, item) {
			t.Fatalf("want the dry-run log to list %q, got:\n%s", item, dryRun)
		}
	}

	// A flow that extracts nothing does not work, even though nothing threw:
	// its dry run is not ok (spec: "A dry run that extracted no items is not
	// ok"). empty.json fetches statically, so the core itself runs the XPath
	// and finds zero elements, rather than the agent failing on it.
	emptyRun := cli.MustRun(ctx, 1, "", "source", "run", sourceID, "-R", repoID, "--dry-run", "--flow", emptyFlow)
	if !strings.Contains(emptyRun.Stdout, "dry run extracted no items") {
		t.Fatalf("want the zero-item dry run reported as extracting no items, got:\n%s", emptyRun)
	}

	after := viewSource(ctx, t, cli, repoID, sourceID)
	if !reflect.DeepEqual(before.Flow, after.Flow) {
		t.Fatalf("the dry run changed the saved flow:\nbefore %v\nafter  %v", before.Flow, after.Flow)
	}

	if before.ErrorsInSuccession != after.ErrorsInSuccession {
		t.Fatalf("the dry run changed errorsInSuccession from %d to %d", before.ErrorsInSuccession, after.ErrorsInSuccession)
	}

	t.Log("step 5: saving the fix and running it for real succeeds, and the source is no longer errored")

	cli.MustRun(ctx, 0, "", "source", "update", sourceID, "-R", repoID, "--flow", fixedFlow)

	fixedRun := cli.MustRun(ctx, 0, "", "source", "run", sourceID, "-R", repoID)
	if !strings.Contains(fixedRun.Stdout, "Result: succeeded") {
		t.Fatalf("want a succeeded run summary, got:\n%s", fixedRun)
	}

	if _, stillErrored := erroredSources(ctx, t, cli)[sourceID]; stillErrored {
		t.Fatalf("source %s is still listed by `source list --errored` after a successful run", sourceID)
	}

	t.Log("step 6: a PATCH with a stale ETag is refused with 412")

	sourcePath := fmt.Sprintf("repositories/%s/sources/%s", repoID, sourceID)

	etag := ResponseHeader(cli.MustRun(ctx, 0, "", "api", "-i", sourcePath), "ETag")
	if etag == "" {
		t.Fatal("GET of the source answered no ETag")
	}

	cli.MustRun(ctx, 0, "", "api", "-X", "PATCH", "-H", "If-Match: "+etag, "-f", "title=fixture items (renamed)", sourcePath)

	stale := cli.Run(ctx, "", "api", "-i", "-X", "PATCH", "-H", "If-Match: "+etag, "-f", "title=lost update", sourcePath)
	if stale.ExitCode == 0 || !strings.Contains(StatusLine(stale), " 412") {
		t.Fatalf("want a PATCH with the stale ETag refused with 412, got:\n%s", stale)
	}
}

type sourceRow struct {
	ID                 string `json:"id"`
	RepositoryID       string `json:"repositoryId"`
	ErrorsInSuccession int    `json:"errorsInSuccession"`
	Flow               any    `json:"flow"`
}

type harvestRow struct {
	ID     string `json:"id"`
	Status string `json:"status"`
	OK     *bool  `json:"ok"`
}

func fixturePath(t *testing.T, elem ...string) string {
	t.Helper()

	path, err := filepath.Abs(filepath.Join(append([]string{"fixtures"}, elem...)...))
	if err != nil {
		t.Fatalf("resolving fixture %v: %v", elem, err)
	}

	return path
}

// createRepository creates an empty repository through `feedctl api` and
// returns its id. The core creates repositories without a next-harvest time,
// so its scheduler harvests every source of a new repository once, a few
// seconds after creation, whatever the refresh cron says (intended
// behaviour). The yearly cron only keeps it from harvesting again during the
// scenario; the scenario waits for that initial harvest to finish
// (waitForHarvestsToSettle) before it runs the source itself.
func createRepository(ctx context.Context, t *testing.T, cli *Feedctl) string {
	t.Helper()

	body := writeJSON(t, map[string]any{
		"product":     "feedless",
		"title":       "feedctl e2e",
		"description": "broken-source fix loop",
		"refreshCron": "0 0 0 1 1 *",
		"sources":     []any{},
	})

	repo := DecodeStdout[struct {
		ID string `json:"id"`
	}](t, cli.MustRun(ctx, 0, "", "api", "-X", "POST", "-H", "Content-Type: application/json", "--input", body, "repositories"))
	if repo.ID == "" {
		t.Fatal("creating the repository answered no id")
	}

	return repo.ID
}

// createSource creates a source running the flow in flowFile in repository
// repoID through `feedctl api` and returns its id.
func createSource(ctx context.Context, t *testing.T, cli *Feedctl, repoID, flowFile string) string {
	t.Helper()

	flow, err := os.ReadFile(flowFile)
	if err != nil {
		t.Fatalf("reading %s: %v", flowFile, err)
	}

	body := writeJSON(t, map[string]any{"title": "fixture items", "flow": json.RawMessage(flow)})

	source := DecodeStdout[sourceRow](t, cli.MustRun(ctx, 0, "",
		"api", "-X", "POST", "-H", "Content-Type: application/json", "--input", body, "repositories/"+repoID+"/sources"))
	if source.ID == "" {
		t.Fatal("creating the source answered no id")
	}

	return source.ID
}

// erroredSources is `feedctl source list --errored` without -R, i.e.
// GET /user/sources across every repository the user can see, keyed by id.
func erroredSources(ctx context.Context, t *testing.T, cli *Feedctl) map[string]sourceRow {
	t.Helper()

	rows := DecodeStdout[[]sourceRow](t, cli.MustRun(ctx, 0, "",
		"source", "list", "--errored", "--json", "id,repositoryId,errorsInSuccession"))

	byID := make(map[string]sourceRow, len(rows))
	for _, row := range rows {
		byID[row.ID] = row
	}

	return byID
}

// settleTimeout bounds each wait for a source's harvests to settle.
const settleTimeout = 2 * time.Minute

// waitForHarvestsToSettle polls the source's real harvests until none is
// queued or running and, when wantCompleted, at least one has completed. It
// fails the test at settleTimeout.
func waitForHarvestsToSettle(ctx context.Context, t *testing.T, cli *Feedctl, repoID, sourceID string, wantCompleted bool) {
	t.Helper()

	deadline := time.Now().Add(settleTimeout)

	for {
		res := cli.RunQuiet(ctx, "", "harvest", "list", "-R", repoID, "-S", sourceID, "--json", "id,status")
		if res.ExitCode != 0 {
			t.Fatalf("listing the source's harvests:\n%s", res)
		}

		busy, completed := 0, 0

		for _, h := range DecodeStdout[[]harvestRow](t, res) {
			switch h.Status {
			case "queued", "running":
				busy++
			case "completed":
				completed++
			}
		}

		if busy == 0 && (!wantCompleted || completed > 0) {
			t.Logf("harvests of source %s settled: %d completed, none queued or running", sourceID, completed)

			return
		}

		if time.Now().After(deadline) {
			t.Fatalf("harvests of source %s did not settle within %s: %d queued or running, %d completed",
				sourceID, settleTimeout, busy, completed)
		}

		select {
		case <-ctx.Done():
			t.Fatalf("waiting for the harvests of source %s to settle: %v", sourceID, ctx.Err())
		case <-time.After(time.Second):
		}
	}
}

func viewSource(ctx context.Context, t *testing.T, cli *Feedctl, repoID, sourceID string) sourceRow {
	t.Helper()

	return DecodeStdout[sourceRow](t, cli.MustRun(ctx, 0, "",
		"source", "view", sourceID, "-R", repoID, "--json", "id,repositoryId,errorsInSuccession,flow"))
}

func writeJSON(t *testing.T, v any) string {
	t.Helper()

	data, err := json.Marshal(v)
	if err != nil {
		t.Fatalf("encoding request body: %v", err)
	}

	path := filepath.Join(t.TempDir(), "body.json")
	if err := os.WriteFile(path, data, 0o600); err != nil {
		t.Fatalf("writing request body: %v", err)
	}

	return path
}
