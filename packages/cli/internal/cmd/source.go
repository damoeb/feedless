package cmd

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/config"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newSourceCmd builds the `feedctl source` command group: list, view, and
// update. Creating and deleting sources is out of scope for this slice
// (the brief reserves source create|delete for later).
func newSourceCmd(version string) *cobra.Command {
	source := &cobra.Command{
		Use:   "source",
		Short: "Inspect and fix sources",
	}

	source.AddCommand(newSourceListCmd(version))
	source.AddCommand(newSourceViewCmd(version))
	source.AddCommand(newSourceUpdateCmd(version))

	return source
}

// sourceListJSONFields and sourceViewJSONFields are the field names --json
// accepts for `source list` and `source view` respectively (see
// output.AddJSONFlags): view adds "flow" on top of list's set, matching
// "--json prints the source" (requirement 2) — the full Source, not just
// the summary columns view's textual output shows.
var (
	sourceListJSONFields = []string{
		"id", "title", "repositoryId", "disabled", "errorsInSuccession", "lastRefreshedAt", "lastErrorMessage", "tags",
	}
	sourceViewJSONFields = append(append([]string{}, sourceListJSONFields...), "flow")
)

// --- list ---

func newSourceListCmd(version string) *cobra.Command {
	var search string
	var disabled bool
	var errored int

	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List sources, most broken first when filtered by --errored",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runSourceList(cmd, version, sourceListFlags{
				search:          search,
				disabled:        disabled,
				disabledChanged: cmd.Flags().Changed("disabled"),
				errored:         errored,
				erroredChanged:  cmd.Flags().Changed("errored"),
			})
		},
	}

	addRepoFlag(listCmd)
	output.AddJSONFlags(listCmd, sourceListJSONFields)
	output.AddLimitFlag(listCmd, 30)

	flags := listCmd.Flags()
	flags.BoolVar(&disabled, "disabled", false, "Only show disabled sources")
	flags.StringVar(&search, "search", "", "Filter by a substring of the source's title/url")
	flags.IntVar(&errored, "errored", 0,
		"Only show sources with at least this many consecutive errors (bare --errored means 1; use --errored=N for N, N>=1)")
	listCmd.Flags().Lookup("errored").NoOptDefVal = "1"

	return listCmd
}

type sourceListFlags struct {
	search          string
	disabled        bool
	disabledChanged bool
	errored         int
	erroredChanged  bool
}

func runSourceList(cmd *cobra.Command, version string, f sourceListFlags) error {
	if f.erroredChanged && f.errored < 1 {
		return errors.New("--errored must be >= 1")
	}

	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	limit, err := output.ReadLimitFlag(cmd)
	if err != nil {
		return err
	}

	repoIDStr, err := resolveRepo(cmd, false)
	if err != nil {
		return err
	}

	withRepo := repoIDStr != ""

	var repoID api.RepositoryId
	if withRepo {
		repoID, err = uuid.Parse(repoIDStr)
		if err != nil {
			return fmt.Errorf("invalid -R/--repo %q: %w", repoIDStr, err)
		}
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	var minErrors *int
	if f.erroredChanged {
		v := f.errored
		minErrors = &v
	}

	var disabledFilter *bool
	if f.disabledChanged {
		v := f.disabled
		disabledFilter = &v
	}

	var like *string
	if f.search != "" {
		like = &f.search
	}

	items, err := output.Paginate(limit, func(page, pageSize int) (output.Page[api.Source], error) {
		return fetchSourcesPage(cmd, apiClient, withRepo, repoID, page, pageSize, disabledFilter, like, minErrors)
	})
	if err != nil {
		return err
	}

	if jf.Requested {
		rows := make([]output.Row, len(items))
		for i, s := range items {
			rows[i] = sourceRow(s, false)
		}

		return output.PrintJSONList(cmd.OutOrStdout(), rows, jf.Fields, jf.JQ)
	}

	return renderSourceTable(cmd, items, withRepo)
}

// fetchSourcesPage fetches one page of sources for output.Paginate: the
// per-repository endpoint (GET /repositories/{r}/sources) when withRepo,
// otherwise GET /user/sources across every repository the caller can see —
// requirement 1's "With -R: per-repository endpoint; without: /user/sources".
func fetchSourcesPage(
	cmd *cobra.Command, apiClient *client.Client, withRepo bool, repoID api.RepositoryId,
	page, pageSize int, disabled *bool, like *string, minErrors *int,
) (output.Page[api.Source], error) {
	if withRepo {
		resp, err := apiClient.API.ListSourcesWithResponse(cmd.Context(), repoID, &api.ListSourcesParams{
			Page: &page, PageSize: &pageSize, Disabled: disabled, Like: like, MinErrorsInSuccession: minErrors,
		})
		if err != nil {
			return output.Page[api.Source]{}, err
		}
		if apiErr := NewAPIError(resp, ""); apiErr != nil {
			return output.Page[api.Source]{}, apiErr
		}

		return output.Page[api.Source]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
	}

	resp, err := apiClient.API.ListUserSourcesWithResponse(cmd.Context(), &api.ListUserSourcesParams{
		Page: &page, PageSize: &pageSize, Disabled: disabled, Like: like, MinErrorsInSuccession: minErrors,
	})
	if err != nil {
		return output.Page[api.Source]{}, err
	}
	if apiErr := NewAPIError(resp, ""); apiErr != nil {
		return output.Page[api.Source]{}, apiErr
	}

	return output.Page[api.Source]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
}

func renderSourceTable(cmd *cobra.Command, items []api.Source, withRepo bool) error {
	isTTY := output.IsTerminal(os.Stdout)
	tp := output.NewTablePrinter(cmd.OutOrStdout(), isTTY, output.TerminalWidth(os.Stdout))

	if isTTY {
		tp.AddField("ID", output.WithColor(output.Bold))
		tp.AddField("TITLE", output.WithColor(output.Bold))
		if !withRepo {
			tp.AddField("REPO", output.WithColor(output.Bold))
		}
		tp.AddField("ERRORS", output.WithColor(output.Bold))
		tp.AddField("LAST RUN", output.WithColor(output.Bold))
		tp.AddField("LAST ERROR", output.WithColor(output.Bold))
		tp.EndRow()
	}

	for _, s := range items {
		tp.AddField(s.Id.String())
		tp.AddField(s.Title)
		if !withRepo {
			tp.AddField(s.RepositoryId.String())
		}
		tp.AddField(strconv.Itoa(s.ErrorsInSuccession))
		tp.AddField(formatLastRun(s.LastRefreshedAt, isTTY))
		tp.AddField(firstLine(s.LastErrorMessage))
		tp.EndRow()
	}

	return tp.Render()
}

// --- view ---

func newSourceViewCmd(version string) *cobra.Command {
	viewCmd := &cobra.Command{
		Use:   "view <id>",
		Short: "Show a source, including its flow as a numbered action list",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runSourceView(cmd, version, args[0])
		},
	}

	addRepoFlag(viewCmd)
	output.AddJSONFlags(viewCmd, sourceViewJSONFields)

	return viewCmd
}

func runSourceView(cmd *cobra.Command, version, idArg string) error {
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

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	resp, err := apiClient.API.GetSourceWithResponse(cmd.Context(), repoID, sourceID)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
		return apiErr
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), sourceRow(*resp.JSON200, true), jf.Fields, jf.JQ)
	}

	return renderSourceView(cmd.OutOrStdout(), *resp.JSON200)
}

// renderSourceView prints s the way `source view` and a successful `source
// update` (editor or field-flag path alike) both render it: title,
// repository, disabled, errors in succession, last run, last error (in
// full — never truncated, unlike the list table's LAST ERROR column), tags,
// and the flow as a numbered action list.
func renderSourceView(w io.Writer, s api.Source) error {
	disabled := s.Disabled != nil && *s.Disabled

	fields := []struct{ label, value string }{
		{"Title", s.Title},
		{"Repository", s.RepositoryId.String()},
		{"Disabled", strconv.FormatBool(disabled)},
		{"Errors in succession", strconv.Itoa(s.ErrorsInSuccession)},
		{"Last run", formatLastRun(s.LastRefreshedAt, false)},
		{"Last error", stringOrDash(s.LastErrorMessage)},
		{"Tags", tagsOrDash(s.Tags)},
	}

	for _, f := range fields {
		if _, err := fmt.Fprintf(w, "%s: %s\n", f.label, f.value); err != nil {
			return fmt.Errorf("writing source view: %w", err)
		}
	}

	if _, err := fmt.Fprintln(w, "Flow:"); err != nil {
		return fmt.Errorf("writing source view: %w", err)
	}

	actions := flowActionLines(s.Flow)
	if len(actions) == 0 {
		_, err := fmt.Fprintln(w, "  (empty)")
		if err != nil {
			return fmt.Errorf("writing source view: %w", err)
		}

		return nil
	}

	for _, a := range actions {
		if _, err := fmt.Fprintf(w, "  %s\n", a); err != nil {
			return fmt.Errorf("writing source view: %w", err)
		}
	}

	return nil
}

// --- update ---

func newSourceUpdateCmd(version string) *cobra.Command {
	var title string
	var tagsRaw string
	var disabled bool
	var flowPath string
	var useEditor bool

	updateCmd := &cobra.Command{
		Use:   "update <id>",
		Short: "Update a source's fields or flow",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runSourceUpdate(cmd, version, args[0], sourceUpdateFlags{
				title:           title,
				titleChanged:    cmd.Flags().Changed("title"),
				tags:            tagsRaw,
				tagsChanged:     cmd.Flags().Changed("tags"),
				disabled:        disabled,
				disabledChanged: cmd.Flags().Changed("disabled"),
				flowPath:        flowPath,
				flowChanged:     cmd.Flags().Changed("flow"),
				useEditor:       useEditor,
			}, launchSystemEditor)
		},
	}

	addRepoFlag(updateCmd)

	flags := updateCmd.Flags()
	flags.StringVar(&title, "title", "", "New title")
	flags.StringVar(&tagsRaw, "tags", "", "Comma-separated tags, replacing the current set")
	flags.BoolVar(&disabled, "disabled", false, "Set (or, with --disabled=false, clear) the source's disabled flag")
	flags.StringVar(&flowPath, "flow", "",
		"Replace the flow with the ScrapeFlow JSON read from this file, or - for stdin (mutually exclusive with --editor)")
	flags.BoolVar(&useEditor, "editor", false,
		"Edit the flow interactively in $VISUAL/$EDITOR/vi, kubectl-edit style (mutually exclusive with --flow)")

	return updateCmd
}

type sourceUpdateFlags struct {
	title           string
	titleChanged    bool
	tags            string
	tagsChanged     bool
	disabled        bool
	disabledChanged bool
	flowPath        string
	flowChanged     bool
	useEditor       bool
}

// sourceFieldOverrides is the subset of SourceUpdate driven by field flags
// (--title/--tags/--disabled) — shared between the direct-PATCH path
// (runSourceUpdate) and the editor loop (runFlowEditor), since "other flags
// may be combined with --editor; they go into the same PATCH" (brief,
// requirement 3).
type sourceFieldOverrides struct {
	title    *string
	tags     *[]string
	disabled *bool
}

func runSourceUpdate(cmd *cobra.Command, version, idArg string, f sourceUpdateFlags, editor editorFunc) error {
	if f.useEditor && f.flowChanged {
		return errors.New("--editor and --flow are mutually exclusive")
	}

	if !f.useEditor && !f.flowChanged && !f.titleChanged && !f.tagsChanged && !f.disabledChanged {
		return errors.New("nothing to update: give --title, --tags, --disabled, --flow, or --editor")
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	sourceID, err := parseSourceID(idArg)
	if err != nil {
		return err
	}

	var flowFromFile *api.ScrapeFlow
	if f.flowChanged {
		flow, flowErr := readScrapeFlow(cmd, f.flowPath)
		if flowErr != nil {
			return flowErr
		}

		flowFromFile = &flow
	}

	overrides := sourceFieldOverrides{}
	if f.titleChanged {
		t := f.title
		overrides.title = &t
	}
	if f.tagsChanged {
		tags := splitTags(f.tags)
		overrides.tags = &tags
	}
	if f.disabledChanged {
		d := f.disabled
		overrides.disabled = &d
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	if f.useEditor {
		return runFlowEditor(cmd, apiClient, repoID, sourceID, overrides, editor)
	}

	return applyFieldUpdate(cmd, apiClient, repoID, sourceID, overrides, flowFromFile)
}

// applyFieldUpdate sends one PATCH built from field flags and/or --flow,
// without an If-Match header — "no If-Match: last write wins" (brief,
// requirement 3).
func applyFieldUpdate(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	overrides sourceFieldOverrides, flow *api.ScrapeFlow,
) error {
	patch := api.SourceUpdate{Title: overrides.title, Tags: overrides.tags, Disabled: overrides.disabled, Flow: flow}

	resp, err := apiClient.API.UpdateSourceWithResponse(cmd.Context(), repoID, sourceID, nil, patch)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
		return apiErr
	}

	return renderSourceView(cmd.OutOrStdout(), *resp.JSON200)
}

// readScrapeFlow reads --flow's value: path's file content, or stdin when
// path is "-", parsed as a ScrapeFlow ({"sequence": [...]}). Invalid JSON
// fails here — a local error before any request, per the brief.
func readScrapeFlow(cmd *cobra.Command, path string) (api.ScrapeFlow, error) {
	var data []byte
	var err error

	if path == "-" {
		data, err = io.ReadAll(cmd.InOrStdin())
	} else {
		data, err = os.ReadFile(path)
	}

	if err != nil {
		return api.ScrapeFlow{}, fmt.Errorf("reading --flow %s: %w", path, err)
	}

	var flow api.ScrapeFlow
	if err := json.Unmarshal(data, &flow); err != nil {
		return api.ScrapeFlow{}, fmt.Errorf("invalid --flow JSON: %w", err)
	}

	return flow, nil
}

func splitTags(raw string) []string {
	parts := strings.Split(raw, ",")
	tags := make([]string, 0, len(parts))

	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			tags = append(tags, p)
		}
	}

	return tags
}

// --- shared helpers ---

// newAPIClient resolves config and builds the authenticated client every
// source command uses — the same client.NewFromConfig entry point every
// other command family goes through.
func newAPIClient(cmd *cobra.Command, version string) (*client.Client, error) {
	flagHost, _ := cmd.Flags().GetString("host")

	cfg, err := config.Load()
	if err != nil {
		return nil, err
	}

	apiClient, _, err := client.NewFromConfig(cfg, flagHost, version, cmd.ErrOrStderr())
	if err != nil {
		return nil, err
	}

	return apiClient, nil
}

func parseSourceID(idArg string) (api.SourceId, error) {
	id, err := uuid.Parse(idArg)
	if err != nil {
		return api.SourceId{}, fmt.Errorf("invalid source id %q: %w", idArg, err)
	}

	return id, nil
}

// sourceRow builds a source's --json Row. includeFlow adds the "flow" key
// (view only — list's JSON output stays to the summary fields the table
// shows, matching sourceListJSONFields).
func sourceRow(s api.Source, includeFlow bool) output.Row {
	row := output.Row{
		"id":                 s.Id.String(),
		"repositoryId":       s.RepositoryId.String(),
		"title":              s.Title,
		"disabled":           s.Disabled != nil && *s.Disabled,
		"errorsInSuccession": s.ErrorsInSuccession,
		"lastErrorMessage":   stringOrNil(s.LastErrorMessage),
		"lastRefreshedAt":    timeOrNil(s.LastRefreshedAt),
		"tags":               tagsOrNil(s.Tags),
	}

	if includeFlow {
		row["flow"] = s.Flow
	}

	return row
}

func stringOrNil(s *string) any {
	if s == nil {
		return nil
	}

	return *s
}

func tagsOrNil(tags *[]string) any {
	if tags == nil {
		return nil
	}

	return *tags
}

func timeOrNil(t *time.Time) any {
	if t == nil {
		return nil
	}

	return t.UTC().Format(time.RFC3339)
}

func stringOrDash(s *string) string {
	if s == nil || *s == "" {
		return "-"
	}

	return *s
}

func tagsOrDash(tags *[]string) string {
	if tags == nil || len(*tags) == 0 {
		return "-"
	}

	return strings.Join(*tags, ", ")
}

// firstLine returns s's first line (list's LAST ERROR column shows only
// this, regardless of TTY truncation, which the table printer already
// handles separately), or "-" when s is nil/empty.
func firstLine(s *string) string {
	if s == nil || *s == "" {
		return "-"
	}

	if i := strings.IndexByte(*s, '\n'); i >= 0 {
		return (*s)[:i]
	}

	return *s
}

// formatLastRun renders t as a relative duration on a TTY ("3h ago") or
// RFC 3339 when piped — matching the brief's LAST RUN column spec — and
// "-" when t is nil (never refreshed).
func formatLastRun(t *time.Time, isTTY bool) string {
	if t == nil {
		return "-"
	}

	if isTTY {
		return relativeTime(*t)
	}

	return t.UTC().Format(time.RFC3339)
}

func relativeTime(t time.Time) string {
	d := time.Since(t)

	switch {
	case d < time.Minute:
		return "just now"
	case d < time.Hour:
		return fmt.Sprintf("%dm ago", int(d.Minutes()))
	case d < 24*time.Hour:
		return fmt.Sprintf("%dh ago", int(d.Hours()))
	default:
		return fmt.Sprintf("%dd ago", int(d.Hours()/24))
	}
}
