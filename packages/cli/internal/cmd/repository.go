// repository.go implements `feedctl repo list|view|create|update|delete`
// (task C6). It lives in a separate file from repo.go on purpose: repo.go
// already holds the -R/--repo flag/resolution helpers `source`/`harvest`
// share (added in C4), and reusing that filename for the repository entity
// commands would collide with it — see the task report for the naming
// note. The cobra command group itself is still named "repo"
// (`feedctl repo ...`), matching the brief.
package cmd

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
	"strconv"
	"time"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newRepositoryCmd builds the `feedctl repo` command group: list, view,
// create, update, delete.
func newRepositoryCmd(version string) *cobra.Command {
	repo := &cobra.Command{
		Use:   "repo",
		Short: "Manage repositories",
	}

	repo.AddCommand(newRepositoryListCmd(version))
	repo.AddCommand(newRepositoryViewCmd(version))
	repo.AddCommand(newRepositoryCreateCmd(version))
	repo.AddCommand(newRepositoryUpdateCmd(version))
	repo.AddCommand(newRepositoryDeleteCmd(version))

	return repo
}

// repositoryJSONFields is the field list --json accepts on every repo
// command (list/view/create alike, since — unlike source's "flow" — the
// Repository schema has no single expensive field worth reserving for view
// only): every scalar (and tags, an array, following sourceListJSONFields'
// own precedent) property of the Repository schema.
var repositoryJSONFields = []string{
	"id", "title", "description", "shareKey", "ownerId", "product", "visibility", "refreshCron",
	"tags", "createdAt", "lastUpdatedAt", "nextUpdateAt", "documentCount", "archived",
	"pullsPerMonth", "currentUserIsOwner", "pushNotificationsEnabled",
	"sourcesCount", "sourcesCountWithProblems", "disabledFrom",
}

// --- list ---

func newRepositoryListCmd(version string) *cobra.Command {
	var product, visibility, search string

	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List repositories",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runRepositoryList(cmd, version, repositoryListFlags{product: product, visibility: visibility, search: search})
		},
	}

	output.AddJSONFlags(listCmd, repositoryJSONFields)
	output.AddLimitFlag(listCmd, 30)

	flags := listCmd.Flags()
	flags.StringVar(&product, "product", "", "Filter by product/vertical (or \"all\")")
	flags.StringVar(&visibility, "visibility", "", "Filter by visibility (public|private)")
	flags.StringVar(&search, "search", "", "Filter by a substring of the repository's title/description")

	return listCmd
}

type repositoryListFlags struct {
	product    string
	visibility string
	search     string
}

func runRepositoryList(cmd *cobra.Command, version string, f repositoryListFlags) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	limit, err := output.ReadLimitFlag(cmd)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	var product *api.VerticalFilter
	if f.product != "" {
		p := api.VerticalFilter(f.product)
		product = &p
	}

	var visibility *api.Visibility
	if f.visibility != "" {
		v := api.Visibility(f.visibility)
		visibility = &v
	}

	var q *string
	if f.search != "" {
		q = &f.search
	}

	items, err := output.Paginate(limit, func(page, pageSize int) (output.Page[api.Repository], error) {
		resp, err := apiClient.API.ListRepositoriesWithResponse(cmd.Context(), &api.ListRepositoriesParams{
			Page: &page, PageSize: &pageSize, Product: product, Visibility: visibility, Q: q,
		})
		if err != nil {
			return output.Page[api.Repository]{}, err
		}
		if apiErr := NewAPIError(resp, ""); apiErr != nil {
			return output.Page[api.Repository]{}, apiErr
		}

		return output.Page[api.Repository]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
	})
	if err != nil {
		return err
	}

	if jf.Requested {
		rows := make([]output.Row, len(items))
		for i, r := range items {
			rows[i] = repositoryRow(r)
		}

		return output.PrintJSONList(cmd.OutOrStdout(), rows, jf.Fields, jf.JQ)
	}

	return renderRepositoryTable(cmd, items)
}

func renderRepositoryTable(cmd *cobra.Command, items []api.Repository) error {
	isTTY := output.IsTerminal(os.Stdout)
	tp := output.NewTablePrinter(cmd.OutOrStdout(), isTTY, output.TerminalWidth(os.Stdout))

	if isTTY {
		tp.AddField("ID", output.WithColor(output.Bold))
		tp.AddField("TITLE", output.WithColor(output.Bold))
		tp.AddField("PRODUCT", output.WithColor(output.Bold))
		tp.AddField("VISIBILITY", output.WithColor(output.Bold))
		tp.AddField("CRON", output.WithColor(output.Bold))
		tp.AddField("UPDATED", output.WithColor(output.Bold))
		tp.EndRow()
	}

	for _, r := range items {
		lastUpdated := r.LastUpdatedAt

		tp.AddField(r.Id.String())
		tp.AddField(r.Title)
		tp.AddField(string(r.Product))
		tp.AddField(string(r.Visibility))
		tp.AddField(emptyOrDash(r.RefreshCron))
		tp.AddField(formatLastRun(&lastUpdated, isTTY))
		tp.EndRow()
	}

	return tp.Render()
}

// --- view ---

func newRepositoryViewCmd(version string) *cobra.Command {
	viewCmd := &cobra.Command{
		Use:   "view <id>",
		Short: "Show a repository",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRepositoryView(cmd, version, args[0])
		},
	}

	output.AddJSONFlags(viewCmd, repositoryJSONFields)

	return viewCmd
}

func runRepositoryView(cmd *cobra.Command, version, idArg string) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	repoID, err := parseRepositoryID(idArg)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	resp, err := apiClient.API.GetRepositoryWithResponse(cmd.Context(), repoID)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("repository %s", repoID)); apiErr != nil {
		return apiErr
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), repositoryRow(*resp.JSON200), jf.Fields, jf.JQ)
	}

	return renderRepositoryView(cmd.OutOrStdout(), *resp.JSON200)
}

// renderRepositoryView prints r the way `repo view` and a successful `repo
// update` (editor or field-flag path alike) both render it: title,
// description, product, visibility, cron, retention, created/updated/next
// update, archived, and document count — matching the brief's field list.
//
// Retention always prints "-": GET /repositories (openapi.yaml's
// Repository schema) never returns it — only RepositoryCreate/
// RepositoryUpdate accept it — so there is nothing to show here. Flagged in
// the task report; fixing it is a server-side (http-api) schema change,
// out of this task's scope.
func renderRepositoryView(w io.Writer, r api.Repository) error {
	fields := []struct{ label, value string }{
		{"Title", r.Title},
		{"Description", emptyOrDash(r.Description)},
		{"Product", string(r.Product)},
		{"Visibility", string(r.Visibility)},
		{"Cron", emptyOrDash(r.RefreshCron)},
		{"Retention", "-"},
		{"Created", r.CreatedAt.UTC().Format(time.RFC3339)},
		{"Updated", r.LastUpdatedAt.UTC().Format(time.RFC3339)},
		{"Next update", formatLastRun(r.NextUpdateAt, false)},
		{"Archived", strconv.FormatBool(r.Archived)},
		{"Document count", documentCountOrDash(r.DocumentCount)},
	}

	for _, f := range fields {
		if _, err := fmt.Fprintf(w, "%s: %s\n", f.label, output.SafeText(f.value)); err != nil {
			return fmt.Errorf("writing repository view: %w", err)
		}
	}

	return nil
}

// --- create ---

func newRepositoryCreateCmd(version string) *cobra.Command {
	var title, description, product, cron, visibility, inputPath string

	createCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a repository",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runRepositoryCreate(cmd, version, repositoryCreateFlags{
				title:             title,
				titleChanged:      cmd.Flags().Changed("title"),
				description:       description,
				descriptionChange: cmd.Flags().Changed("description"),
				product:           product,
				productChanged:    cmd.Flags().Changed("product"),
				cron:              cron,
				cronChanged:       cmd.Flags().Changed("cron"),
				visibility:        visibility,
				visibilityChanged: cmd.Flags().Changed("visibility"),
				inputPath:         inputPath,
				inputChanged:      cmd.Flags().Changed("input"),
			})
		},
	}

	output.AddJSONFlags(createCmd, repositoryJSONFields)

	flags := createCmd.Flags()
	flags.StringVar(&title, "title", "", "Title (required unless --input)")
	flags.StringVar(&description, "description", "", "Description")
	flags.StringVar(&product, "product", "", "Product/vertical (required unless --input)")
	flags.StringVar(&cron, "cron", "", "Refresh cron expression")
	flags.StringVar(&visibility, "visibility", "", "Visibility (public|private)")
	flags.StringVar(&inputPath, "input", "",
		"Read a full RepositoryCreate JSON document from this file, or - for stdin (mutually exclusive with the field flags)")

	return createCmd
}

type repositoryCreateFlags struct {
	title             string
	titleChanged      bool
	description       string
	descriptionChange bool
	product           string
	productChanged    bool
	cron              string
	cronChanged       bool
	visibility        string
	visibilityChanged bool
	inputPath         string
	inputChanged      bool
}

func runRepositoryCreate(cmd *cobra.Command, version string, f repositoryCreateFlags) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	usesFieldFlags := f.titleChanged || f.descriptionChange || f.productChanged || f.cronChanged || f.visibilityChanged

	if f.inputChanged && usesFieldFlags {
		return errors.New("--input and the field flags (--title, --description, --product, --cron, --visibility) are mutually exclusive")
	}

	var body api.RepositoryCreate

	if f.inputChanged {
		body, err = readRepositoryCreate(cmd, f.inputPath)
		if err != nil {
			return err
		}
	} else {
		if !f.titleChanged {
			return errors.New("--title is required unless --input is given")
		}
		if !f.productChanged {
			return errors.New("--product is required unless --input is given")
		}

		body = api.RepositoryCreate{
			Title:       f.title,
			Description: f.description,
			Product:     api.Vertical(f.product),
			Sources:     []api.SourceCreate{},
		}

		if f.cronChanged {
			c := f.cron
			body.RefreshCron = &c
		}
		if f.visibilityChanged {
			v := api.Visibility(f.visibility)
			body.Visibility = &v
		}
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	resp, err := apiClient.API.CreateRepositoryWithResponse(cmd.Context(), body)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, ""); apiErr != nil {
		return apiErr
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), repositoryRow(*resp.JSON201), jf.Fields, jf.JQ)
	}

	return renderRepositoryView(cmd.OutOrStdout(), *resp.JSON201)
}

// readRepositoryCreate reads --input's value: path's file content, or
// stdin when path is "-", parsed as a full RepositoryCreate document.
// Invalid JSON fails here — a local error before any request.
func readRepositoryCreate(cmd *cobra.Command, path string) (api.RepositoryCreate, error) {
	var data []byte
	var err error

	if path == "-" {
		data, err = io.ReadAll(cmd.InOrStdin())
	} else {
		data, err = os.ReadFile(path)
	}

	if err != nil {
		return api.RepositoryCreate{}, fmt.Errorf("reading --input %s: %w", path, err)
	}

	var body api.RepositoryCreate
	if err := json.Unmarshal(data, &body); err != nil {
		return api.RepositoryCreate{}, fmt.Errorf("invalid --input JSON: %w", err)
	}

	return body, nil
}

// --- update ---

func newRepositoryUpdateCmd(version string) *cobra.Command {
	var title, description, cron, visibility string
	var useEditor bool

	updateCmd := &cobra.Command{
		Use:   "update <id>",
		Short: "Update a repository's fields",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRepositoryUpdate(cmd, version, args[0], repositoryUpdateFlags{
				title:             title,
				titleChanged:      cmd.Flags().Changed("title"),
				description:       description,
				descriptionChange: cmd.Flags().Changed("description"),
				cron:              cron,
				cronChanged:       cmd.Flags().Changed("cron"),
				visibility:        visibility,
				visibilityChanged: cmd.Flags().Changed("visibility"),
				useEditor:         useEditor,
			}, launchSystemEditor)
		},
	}

	flags := updateCmd.Flags()
	flags.StringVar(&title, "title", "", "New title")
	flags.StringVar(&description, "description", "", "New description")
	flags.StringVar(&cron, "cron", "", "New refresh cron expression")
	flags.StringVar(&visibility, "visibility", "", "New visibility (public|private)")
	flags.BoolVar(&useEditor, "editor", false,
		"Edit the repository's updatable fields interactively in $VISUAL/$EDITOR/vi, kubectl-edit style")

	return updateCmd
}

type repositoryUpdateFlags struct {
	title             string
	titleChanged      bool
	description       string
	descriptionChange bool
	cron              string
	cronChanged       bool
	visibility        string
	visibilityChanged bool
	useEditor         bool
}

// repositoryFieldOverrides is the subset of RepositoryUpdate driven by
// field flags (--title/--description/--cron/--visibility) — shared between
// the direct-PATCH path (runRepositoryUpdate) and the editor loop
// (runRepositoryEditor in repository_editor.go), mirroring
// sourceFieldOverrides: a field flag always wins over whatever the same
// field held in the edited JSON document, applied after parsing it.
type repositoryFieldOverrides struct {
	title       *string
	description *string
	refreshCron *string
	visibility  *api.Visibility
}

func runRepositoryUpdate(cmd *cobra.Command, version, idArg string, f repositoryUpdateFlags, editor editorFunc) error {
	if !f.useEditor && !f.titleChanged && !f.descriptionChange && !f.cronChanged && !f.visibilityChanged {
		return errors.New("nothing to update: give --title, --description, --cron, --visibility, or --editor")
	}

	repoID, err := parseRepositoryID(idArg)
	if err != nil {
		return err
	}

	overrides := repositoryFieldOverrides{}
	if f.titleChanged {
		t := f.title
		overrides.title = &t
	}
	if f.descriptionChange {
		d := f.description
		overrides.description = &d
	}
	if f.cronChanged {
		c := f.cron
		overrides.refreshCron = &c
	}
	if f.visibilityChanged {
		v := api.Visibility(f.visibility)
		overrides.visibility = &v
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	if f.useEditor {
		return runRepositoryEditor(cmd, apiClient, repoID, overrides, editor)
	}

	return applyRepositoryFieldUpdate(cmd, apiClient, repoID, overrides)
}

// applyRepositoryFieldUpdate sends one PATCH built purely from field flags,
// without an If-Match header — "no If-Match: last write wins", mirroring
// source update's non-editor path.
func applyRepositoryFieldUpdate(cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, overrides repositoryFieldOverrides) error {
	patch := repositoryOverridesToUpdate(overrides)

	resp, err := apiClient.API.UpdateRepositoryWithResponse(cmd.Context(), repoID, nil, patch)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("repository %s", repoID)); apiErr != nil {
		return apiErr
	}

	return renderRepositoryView(cmd.OutOrStdout(), *resp.JSON200)
}

func repositoryOverridesToUpdate(o repositoryFieldOverrides) api.RepositoryUpdate {
	return api.RepositoryUpdate{
		Title:       o.title,
		Description: o.description,
		RefreshCron: o.refreshCron,
		Visibility:  o.visibility,
	}
}

// --- delete ---

func newRepositoryDeleteCmd(version string) *cobra.Command {
	var yes bool

	deleteCmd := &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a repository",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRepositoryDelete(cmd, version, args[0], yes)
		},
	}

	deleteCmd.Flags().BoolVar(&yes, "yes", false, "Skip the confirmation prompt")

	return deleteCmd
}

func runRepositoryDelete(cmd *cobra.Command, version, idArg string, yes bool) error {
	repoID, err := parseRepositoryID(idArg)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	return deleteRepository(cmd, apiClient, repoID, stdinIsTTY(cmd.InOrStdin()), yes)
}

// deleteRepository is `repo delete`'s actual logic: fetch the title (unless
// --yes, in which case the prompt — and so the GET needed only to name it —
// is skipped entirely, mirroring C8's source delete), confirm via
// confirmDelete, then DELETE.
//
// Kept independent of cobra flag parsing and of
// client.NewFromConfig/config.Load, and takes isTTY as an explicit
// parameter rather than detecting it itself, so it can be exercised
// directly against an httptest server with an injected isTTY/yes/stdin
// combination — including the TTY-accepted and TTY-declined paths, which a
// full cobra Execute() can never reach in a test (test stdin is never a
// real *os.File) — see repository_test.go's TestDeleteRepository_* cases.
func deleteRepository(cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, isTTY, yes bool) error {
	// Fetching the title first so the prompt names it — skipped entirely
	// with --yes (the prompt itself is skipped then too), and also skipped
	// for the non-TTY-without-yes case: confirmDelete refuses that
	// unconditionally, so checking it here first (with a throwaway prompt
	// it never uses) means that refusal happens before any request, the
	// same as every other "fail fast, before touching the network" guard
	// elsewhere in this package.
	title := repoID.String()

	if !yes {
		if !isTTY {
			return confirmDelete(cmd, isTTY, yes, "")
		}

		resp, getErr := apiClient.API.GetRepositoryWithResponse(cmd.Context(), repoID)
		if getErr != nil {
			return getErr
		}
		if apiErr := NewAPIError(resp, fmt.Sprintf("repository %s", repoID)); apiErr != nil {
			return apiErr
		}

		title = output.SafeText(resp.JSON200.Title)
	}

	prompt := fmt.Sprintf("Delete repository %s (%s)?", title, repoID)
	if err := confirmDelete(cmd, isTTY, yes, prompt); err != nil {
		return err
	}

	delResp, err := apiClient.API.DeleteRepositoryWithResponse(cmd.Context(), repoID)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(delResp, fmt.Sprintf("repository %s", repoID)); apiErr != nil {
		return apiErr
	}

	_, _ = fmt.Fprintf(cmd.OutOrStdout(), "deleted %s\n", repoID)

	return nil
}

// --- shared helpers ---

func parseRepositoryID(idArg string) (api.RepositoryId, error) {
	id, err := uuid.Parse(idArg)
	if err != nil {
		return api.RepositoryId{}, fmt.Errorf("invalid repository id %q: %w", idArg, err)
	}

	return id, nil
}

// repositoryRow builds a repository's --json Row, covering every field in
// repositoryJSONFields.
func repositoryRow(r api.Repository) output.Row {
	return output.Row{
		"id":                       r.Id.String(),
		"title":                    r.Title,
		"description":              r.Description,
		"shareKey":                 stringOrNil(r.ShareKey),
		"ownerId":                  r.OwnerId.String(),
		"product":                  string(r.Product),
		"visibility":               string(r.Visibility),
		"refreshCron":              r.RefreshCron,
		"tags":                     r.Tags,
		"createdAt":                r.CreatedAt.UTC().Format(time.RFC3339),
		"lastUpdatedAt":            r.LastUpdatedAt.UTC().Format(time.RFC3339),
		"nextUpdateAt":             timeOrNil(r.NextUpdateAt),
		"documentCount":            int64OrNil(r.DocumentCount),
		"archived":                 r.Archived,
		"pullsPerMonth":            intOrNil(r.PullsPerMonth),
		"currentUserIsOwner":       boolOrNil(r.CurrentUserIsOwner),
		"pushNotificationsEnabled": boolOrNil(r.PushNotificationsEnabled),
		"sourcesCount":             intOrNil(r.SourcesCount),
		"sourcesCountWithProblems": intOrNil(r.SourcesCountWithProblems),
		"disabledFrom":             timeOrNil(r.DisabledFrom),
	}
}

// intOrNil and boolOrNil are harvest.go's — reused here rather than
// redeclared (same *int/*bool -> any nil-or-value shape).

func int64OrNil(v *int64) any {
	if v == nil {
		return nil
	}

	return *v
}

func emptyOrDash(s string) string {
	if s == "" {
		return "-"
	}

	return s
}

func documentCountOrDash(v *int64) string {
	if v == nil {
		return "-"
	}

	return strconv.FormatInt(*v, 10)
}
