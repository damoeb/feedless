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

// Split from runSourceRun so tests can inject a fake sleeper and clock.
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

	return runSourceRun(cmd, apiClient, repoID, sourceID, f, flow, jf, productionPollDeps(cmd.ErrOrStderr()))
}

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
			// run() replaces our error with its own "cancelled" message, so the hint must be printed here.
			_, _ = fmt.Fprintf(cmd.ErrOrStderr(), "harvest %s keeps running on the server — see: feedctl harvest view %s -R %s -S %s\n",
				queued.Id, queued.Id, repoID, sourceID)
		}

		return waitErr
	}

	return reportHarvestOutcome(cmd, apiClient, repoID, sourceID, final, jf)
}

func reportQueuedHarvest(cmd *cobra.Command, queued api.Harvest, jf output.JSONFlags) error {
	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), harvestRow(queued), jf.Fields, jf.JQ)
	}

	if _, err := fmt.Fprintln(cmd.OutOrStdout(), queued.Id.String()); err != nil {
		return fmt.Errorf("writing harvest id: %w", err)
	}

	return nil
}

// A failed dry run is the expected case: the summary on stdout says it all, stderr stays clean.
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

type harvestNotOKError struct {
	id api.HarvestId
}

func (e *harvestNotOKError) Error() string { return fmt.Sprintf("harvest %s did not succeed", e.id) }

func (e *harvestNotOKError) RenderError(_ io.Writer) {}

func (e *harvestNotOKError) ExitCode() int { return 1 }
