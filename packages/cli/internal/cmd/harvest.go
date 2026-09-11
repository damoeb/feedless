package cmd

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"strconv"
	"time"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newHarvestCmd builds the `feedctl harvest` command group: list and view.
// Running a source (`source run`) lives on the `source` command group
// instead — see source_run.go — but shares this file's rendering helpers
// (harvestRow, printHarvestSummary, printHarvestLog) since both print the
// same Harvest shape.
func newHarvestCmd(version string) *cobra.Command {
	harvest := &cobra.Command{
		Use:   "harvest",
		Short: "Inspect harvests (source runs, dry or real)",
	}

	harvest.AddCommand(newHarvestListCmd(version))
	harvest.AddCommand(newHarvestViewCmd(version))

	return harvest
}

// harvestJSONFields is the field list --json accepts for a Harvest — shared
// by `harvest list`, `harvest view`, and `source run` (see source_run.go),
// since all three print the same shape.
var harvestJSONFields = []string{
	"id", "sourceId", "status", "dryRun", "ok", "itemsAdded", "itemsIgnored", "startedAt", "finishedAt",
}

// --- list ---

func newHarvestListCmd(version string) *cobra.Command {
	var dryRun bool

	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List a source's harvests, newest first",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runHarvestList(cmd, version, dryRun)
		},
	}

	addRepoFlag(listCmd)
	addSourceFlag(listCmd)
	output.AddJSONFlags(listCmd, harvestJSONFields)
	output.AddLimitFlag(listCmd, 30)
	listCmd.Flags().BoolVar(&dryRun, "dry-run", false, "List dry runs instead of real runs")

	return listCmd
}

func runHarvestList(cmd *cobra.Command, version string, dryRun bool) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	limit, err := output.ReadLimitFlag(cmd)
	if err != nil {
		return err
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	sourceID, err := resolveSourceID(cmd)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	items, err := output.Paginate(limit, func(page, pageSize int) (output.Page[api.Harvest], error) {
		return fetchHarvestsPage(cmd, apiClient, repoID, sourceID, page, pageSize, dryRun)
	})
	if err != nil {
		return err
	}

	if jf.Requested {
		rows := make([]output.Row, len(items))
		for i, h := range items {
			rows[i] = harvestRow(h)
		}

		return output.PrintJSONList(cmd.OutOrStdout(), rows, jf.Fields, jf.JQ)
	}

	return renderHarvestTable(cmd, items)
}

// fetchHarvestsPage fetches one page of harvests for output.Paginate. dryRun
// sets the DryRun filter only when true — the server's default (no
// dryRun param) already lists real runs, dry runs only with
// ?dryRun=true (brief, "What was implemented" server notes).
func fetchHarvestsPage(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	page, pageSize int, dryRun bool,
) (output.Page[api.Harvest], error) {
	params := &api.ListHarvestsParams{Page: &page, PageSize: &pageSize}
	if dryRun {
		d := true
		params.DryRun = &d
	}

	resp, err := apiClient.API.ListHarvestsWithResponse(cmd.Context(), repoID, sourceID, params)
	if err != nil {
		return output.Page[api.Harvest]{}, err
	}
	if apiErr := NewAPIError(resp, ""); apiErr != nil {
		return output.Page[api.Harvest]{}, apiErr
	}

	return output.Page[api.Harvest]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
}

func renderHarvestTable(cmd *cobra.Command, items []api.Harvest) error {
	isTTY := output.IsTerminal(os.Stdout)
	tp := output.NewTablePrinter(cmd.OutOrStdout(), isTTY, output.TerminalWidth(os.Stdout))

	if isTTY {
		tp.AddField("ID", output.WithColor(output.Bold))
		tp.AddField("STATUS", output.WithColor(output.Bold))
		tp.AddField("RESULT", output.WithColor(output.Bold))
		tp.AddField("ITEMS", output.WithColor(output.Bold))
		tp.AddField("STARTED", output.WithColor(output.Bold))
		tp.AddField("DURATION", output.WithColor(output.Bold))
		tp.EndRow()
	}

	for _, h := range items {
		startedAt := h.StartedAt

		tp.AddField(h.Id.String())
		tp.AddField(string(h.Status))
		tp.AddField(tableResult(h.Ok))
		tp.AddField(harvestItems(h.ItemsAdded))
		tp.AddField(formatLastRun(&startedAt, isTTY))
		tp.AddField(harvestDuration(h.StartedAt, h.FinishedAt))
		tp.EndRow()
	}

	return tp.Render()
}

// tableResult is `harvest list`'s RESULT column: "ok"/"failed"/"-" (brief,
// requirement 2, exact wording).
func tableResult(ok *bool) string {
	if ok == nil {
		return "-"
	}
	if *ok {
		return "ok"
	}

	return "failed"
}

// --- view ---

func newHarvestViewCmd(version string) *cobra.Command {
	var showLog bool

	viewCmd := &cobra.Command{
		Use:   "view <id>",
		Short: "Show a harvest's summary, or its log with --log",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runHarvestView(cmd, version, args[0], showLog)
		},
	}

	addRepoFlag(viewCmd)
	addSourceFlag(viewCmd)
	output.AddJSONFlags(viewCmd, harvestJSONFields)
	viewCmd.Flags().BoolVar(&showLog, "log", false, "Print only the harvest's log (Accept: text/plain)")

	return viewCmd
}

func runHarvestView(cmd *cobra.Command, version, idArg string, showLog bool) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	sourceID, err := resolveSourceID(cmd)
	if err != nil {
		return err
	}

	harvestID, err := parseHarvestID(idArg)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	if showLog {
		return printHarvestLog(cmd, apiClient, repoID, sourceID, harvestID)
	}

	h, err := fetchHarvest(cmd.Context(), apiClient, repoID, sourceID, harvestID)
	if err != nil {
		return err
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), harvestRow(h), jf.Fields, jf.JQ)
	}

	return printHarvestSummary(cmd.OutOrStdout(), h)
}

// --- shared helpers (source_run.go uses these too) ---

func parseHarvestID(idArg string) (api.HarvestId, error) {
	id, err := uuid.Parse(idArg)
	if err != nil {
		return api.HarvestId{}, fmt.Errorf("invalid harvest id %q: %w", idArg, err)
	}

	return id, nil
}

// fetchHarvest is GET .../harvests/{id}, shared by harvest view and
// source_run.go's poller.
func fetchHarvest(
	ctx context.Context, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId, harvestID api.HarvestId,
) (api.Harvest, error) {
	resp, err := apiClient.API.GetHarvestWithResponse(ctx, repoID, sourceID, harvestID)
	if err != nil {
		return api.Harvest{}, err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("harvest %s", harvestID)); apiErr != nil {
		return api.Harvest{}, apiErr
	}

	return *resp.JSON200, nil
}

// printHarvestLog GETs .../harvests/{id}/logs with Accept: text/plain
// (brief, requirement 5 — the endpoint also produces application/json,
// which would return a JSON-quoted string for a JSON Accept) and writes the
// plain-text body to stdout via sanitizeHarvestLog: sanitized
// (output.SafeText) when stdout is a terminal, verbatim when it's piped —
// so `harvest view --log > file` keeps saving the exact bytes the server
// sent, while a log printed straight to a terminal can't hide/forge output,
// rewrite the terminal title, or write to the clipboard via an embedded
// escape sequence.
func printHarvestLog(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId, harvestID api.HarvestId,
) error {
	resp, err := apiClient.API.GetHarvestLogsWithResponse(cmd.Context(), repoID, sourceID, harvestID, acceptTextPlain)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("harvest %s log", harvestID)); apiErr != nil {
		return apiErr
	}

	body := sanitizeHarvestLog(resp.Body, output.IsTerminal(os.Stdout))

	if _, err := cmd.OutOrStdout().Write(body); err != nil {
		return fmt.Errorf("writing harvest log: %w", err)
	}

	return nil
}

// sanitizeHarvestLog is printHarvestLog's TTY decision as a pure function,
// isTTY passed in rather than detected here — the same "isTTY as an
// explicit, testable parameter" seam poller.go's harvestPoller/pollDeps use
// — so a test can exercise both branches directly, without a real terminal
// (output.IsTerminal(os.Stdout) is always false under `go test`, and per
// this task's constraints, tests must not read a real TTY either).
func sanitizeHarvestLog(body []byte, isTTY bool) []byte {
	if !isTTY {
		return body
	}

	return []byte(output.SafeText(string(body)))
}

func acceptTextPlain(_ context.Context, req *http.Request) error {
	req.Header.Set("Accept", "text/plain")

	return nil
}

// printHarvestSummary writes h's completion summary — result, dry run,
// items, duration (brief, requirement 1: "result (succeeded/failed), dry
// run yes/no, items (itemsAdded), duration") — shared by `source run`
// (after waiting) and `harvest view` (without --log).
func printHarvestSummary(w io.Writer, h api.Harvest) error {
	lines := []struct{ label, value string }{
		{"Result", summaryResult(h.Ok)},
		{"Dry run", yesNo(h.DryRun)},
		{"Items", harvestItems(h.ItemsAdded)},
		{"Duration", harvestDuration(h.StartedAt, h.FinishedAt)},
	}

	for _, l := range lines {
		if _, err := fmt.Fprintf(w, "%s: %s\n", l.label, output.SafeText(l.value)); err != nil {
			return fmt.Errorf("writing harvest summary: %w", err)
		}
	}

	return nil
}

// summaryResult is the summary's "Result" line: "succeeded"/"failed"/"-"
// (brief, requirement 1, exact wording — deliberately different from the
// list table's "ok"/"failed", which requirement 2 spells out separately).
func summaryResult(ok *bool) string {
	if ok == nil {
		return "-"
	}
	if *ok {
		return "succeeded"
	}

	return "failed"
}

func yesNo(b bool) string {
	if b {
		return "yes"
	}

	return "no"
}

func harvestItems(n *int) string {
	if n == nil {
		return "-"
	}

	return strconv.Itoa(*n)
}

// harvestDuration is finished-started, rounded to the second, or "-" while
// the harvest hasn't finished yet (finished is nil for queued/running).
func harvestDuration(started time.Time, finished *time.Time) string {
	if finished == nil {
		return "-"
	}

	return finished.Sub(started).Round(time.Second).String()
}

// harvestRow builds a Harvest's --json Row (harvestJSONFields' key set).
func harvestRow(h api.Harvest) output.Row {
	return output.Row{
		"id":           h.Id.String(),
		"sourceId":     h.SourceId.String(),
		"status":       string(h.Status),
		"dryRun":       h.DryRun,
		"ok":           boolOrNil(h.Ok),
		"itemsAdded":   intOrNil(h.ItemsAdded),
		"itemsIgnored": intOrNil(h.ItemsIgnored),
		"startedAt":    h.StartedAt.UTC().Format(time.RFC3339),
		"finishedAt":   timeOrNil(h.FinishedAt),
	}
}

func boolOrNil(b *bool) any {
	if b == nil {
		return nil
	}

	return *b
}

func intOrNil(n *int) any {
	if n == nil {
		return nil
	}

	return *n
}
