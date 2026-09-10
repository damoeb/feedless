package cmd

import (
	"context"
	"errors"
	"fmt"
	"io"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newSourceRunCmd builds `feedctl source run`: run a source now, or dry-run
// an unsaved --flow to test a fix without saving it — the second half of the
// broken-source fix loop `feedctl source view --errored` /
// `feedctl source update --editor` starts.
func newSourceRunCmd(version string) *cobra.Command {
	var dryRun, noWait bool

	var flowPath string

	runCmd := &cobra.Command{
		Use:   "run <id>",
		Short: "Run a source now, or dry-run an unsaved flow to test a fix",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runSourceRunCmd(cmd, version, args[0], sourceRunFlags{
				dryRun:      dryRun,
				flowPath:    flowPath,
				flowChanged: cmd.Flags().Changed("flow"),
				noWait:      noWait,
			})
		},
	}

	addRepoFlag(runCmd)
	output.AddJSONFlags(runCmd, harvestJSONFields)

	flags := runCmd.Flags()
	flags.BoolVar(&dryRun, "dry-run", false, "Scrape without importing records or touching the source")
	flags.StringVar(&flowPath, "flow", "",
		"Dry-run this ScrapeFlow JSON file instead of the saved flow, or - for stdin (requires --dry-run)")
	flags.BoolVar(&noWait, "no-wait", false,
		"Print the queued harvest id and exit 0 immediately, without waiting for it to complete")

	return runCmd
}

type sourceRunFlags struct {
	dryRun      bool
	flowPath    string
	flowChanged bool
	noWait      bool
}

// runSourceRunCmd resolves flags/config/client from cmd and hands off to
// runSourceRun with production poll dependencies (a real sleeper/clock,
// real stdout TTY detection). Kept separate so tests can call runSourceRun
// directly with a fake sleeper/clock and an httptest-backed client.Client —
// bypassing cobra flag parsing, config.Load, and the keyring — the same
// seam runFlowEditor/source_editor_test.go use for `source update --editor`.
func runSourceRunCmd(cmd *cobra.Command, version, idArg string, f sourceRunFlags) error {
	if f.flowChanged && !f.dryRun {
		return errors.New("--flow requires --dry-run")
	}

	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	sourceID, err := parseSourceID(idArg)
	if err != nil {
		return err
	}

	var flow *api.ScrapeFlow

	if f.flowChanged {
		parsed, flowErr := readScrapeFlow(cmd, f.flowPath)
		if flowErr != nil {
			return flowErr
		}

		flow = &parsed
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	return runSourceRun(cmd, apiClient, repoID, sourceID, f, flow, jf, productionPollDeps())
}

// runSourceRun is `source run`'s core logic: queue the harvest
// (POST .../harvests), then either print its id (--no-wait) or poll it to
// completion (harvestPoller) and report the result. deps carries the
// poller's injectable sleeper/clock/TTY-detection.
func runSourceRun(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	f sourceRunFlags, flow *api.ScrapeFlow, jf output.JSONFlags, deps pollDeps,
) error {
	body := api.HarvestRequest{Flow: flow}
	if f.dryRun {
		d := true
		body.DryRun = &d
	}

	resp, err := apiClient.API.RunSourceWithResponse(cmd.Context(), repoID, sourceID, body)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
		return apiErr
	}

	queued := *resp.JSON202

	if f.noWait {
		return reportQueuedHarvest(cmd, queued, jf)
	}

	poller := &harvestPoller{
		fetch: func(ctx context.Context) (api.Harvest, error) {
			return fetchHarvest(ctx, apiClient, repoID, sourceID, queued.Id)
		},
		sleep:  deps.sleep,
		now:    deps.now,
		stderr: cmd.ErrOrStderr(),
		isTTY:  deps.isTTY,
	}

	final, waitErr := poller.Wait(cmd.Context())
	if waitErr != nil {
		if errors.Is(waitErr, context.Canceled) {
			// main.go's run() maps a cancelled cmd.Context() to exit 2 and its
			// own generic "cancelled" message regardless of what we return here
			// (see cmd/feedctl/main.go's runWithContext) — the required hint
			// must be printed by us, directly, or it never reaches stderr.
			_, _ = fmt.Fprintf(cmd.ErrOrStderr(), "harvest %s keeps running on the server — see: feedctl harvest view %s -R %s -S %s\n",
				queued.Id, queued.Id, repoID, sourceID)
		}

		return waitErr
	}

	return reportHarvestOutcome(cmd, apiClient, repoID, sourceID, final, jf)
}

// reportQueuedHarvest is --no-wait's output: the harvest id on stdout, or
// (with --json) the queued Harvest — always exit 0 (brief, requirement 1).
func reportQueuedHarvest(cmd *cobra.Command, queued api.Harvest, jf output.JSONFlags) error {
	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), harvestRow(queued), jf.Fields, jf.JQ)
	}

	if _, err := fmt.Fprintln(cmd.OutOrStdout(), queued.Id.String()); err != nil {
		return fmt.Errorf("writing harvest id: %w", err)
	}

	return nil
}

// reportHarvestOutcome renders h's result — the summary, plus the log when
// it failed or was a successful dry run (brief, requirement 1) — and turns
// a not-ok result into a non-nil error carrying exit code 1. The error's
// RenderError is a no-op: the summary and log already told the story on
// stdout, so stderr stays clean (a failed dry run is the expected, common
// case this command's fix loop is built around, not a crash to narrate).
func reportHarvestOutcome(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	h api.Harvest, jf output.JSONFlags,
) error {
	ok := h.Ok != nil && *h.Ok

	if jf.Requested {
		if err := output.PrintJSONObject(cmd.OutOrStdout(), harvestRow(h), jf.Fields, jf.JQ); err != nil {
			return err
		}
	} else {
		if err := printHarvestSummary(cmd.OutOrStdout(), h); err != nil {
			return err
		}

		if !ok || h.DryRun {
			if err := printHarvestLog(cmd, apiClient, repoID, sourceID, h.Id); err != nil {
				return err
			}
		}
	}

	if !ok {
		return &harvestNotOKError{id: h.Id}
	}

	return nil
}

// harvestNotOKError is `source run`'s not-ok exit: exit code 1, and no
// stderr output of its own (the summary/log already printed to stdout say
// everything there is to say) — see reportHarvestOutcome.
type harvestNotOKError struct {
	id api.HarvestId
}

func (e *harvestNotOKError) Error() string { return fmt.Sprintf("harvest %s did not succeed", e.id) }

func (e *harvestNotOKError) RenderError(_ io.Writer) {}

func (e *harvestNotOKError) ExitCode() int { return 1 }
