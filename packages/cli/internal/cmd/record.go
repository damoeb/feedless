// record.go implements `feedctl record list|view|create|update|delete`
// (task C7). Every record command is scoped to one repository via -R/--repo
// (addRepoFlag/resolveRepoID, from repo.go — added in C4), since records
// live under /repositories/{repositoryId}/records[/{recordId}]. Attachments,
// raw bodies (rawBase64/rawMimeType), and bulk import are out of scope per
// the brief.
package cmd

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
	"time"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newRecordCmd builds the `feedctl record` command group: list, view,
// create, update, delete.
func newRecordCmd(version string) *cobra.Command {
	record := &cobra.Command{
		Use:   "record",
		Short: "Manage records in a repository",
	}

	record.AddCommand(newRecordListCmd(version))
	record.AddCommand(newRecordViewCmd(version))
	record.AddCommand(newRecordCreateCmd(version))
	record.AddCommand(newRecordUpdateCmd(version))
	record.AddCommand(newRecordDeleteCmd(version))

	return record
}

// recordListJSONFields is `record list`'s --json field list: the Record
// schema's scalar fields only (per the brief), excluding the array field
// "tags" (reserved for view, mirroring how source reserves "flow" to view
// only) and rawBase64/rawMimeType (out of scope — attachments/raw bodies).
var recordListJSONFields = []string{
	"id", "url", "title", "text", "html", "imageUrl", "createdAt", "publishedAt", "updatedAt", "startingAt",
}

// recordViewJSONFields is `record view`'s (and `record create`'s, which
// prints the created record the same way) --json field list: list's fields
// plus "tags" — "--json prints the record" (brief, requirement 2).
var recordViewJSONFields = append(append([]string{}, recordListJSONFields...), "tags")

// --- list ---

func newRecordListCmd(version string) *cobra.Command {
	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List records in a repository",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runRecordList(cmd, version)
		},
	}

	addRepoFlag(listCmd)
	output.AddJSONFlags(listCmd, recordListJSONFields)
	output.AddLimitFlag(listCmd, 30)

	return listCmd
}

func runRecordList(cmd *cobra.Command, version string) error {
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

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	items, err := output.Paginate(limit, func(page, pageSize int) (output.Page[api.Record], error) {
		resp, err := apiClient.API.ListRecordsWithResponse(cmd.Context(), repoID, &api.ListRecordsParams{Page: &page, PageSize: &pageSize})
		if err != nil {
			return output.Page[api.Record]{}, err
		}
		if apiErr := NewAPIError(resp, ""); apiErr != nil {
			return output.Page[api.Record]{}, apiErr
		}

		return output.Page[api.Record]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
	})
	if err != nil {
		return err
	}

	if jf.Requested {
		rows := make([]output.Row, len(items))
		for i, r := range items {
			rows[i] = recordRow(r)
		}

		return output.PrintJSONList(cmd.OutOrStdout(), rows, jf.Fields, jf.JQ)
	}

	return renderRecordTable(cmd, items)
}

func renderRecordTable(cmd *cobra.Command, items []api.Record) error {
	isTTY := output.IsTerminal(os.Stdout)
	tp := output.NewTablePrinter(cmd.OutOrStdout(), isTTY, output.TerminalWidth(os.Stdout))

	if isTTY {
		tp.AddField("ID", output.WithColor(output.Bold))
		tp.AddField("TITLE", output.WithColor(output.Bold))
		tp.AddField("URL", output.WithColor(output.Bold))
		tp.AddField("PUBLISHED", output.WithColor(output.Bold))
		tp.EndRow()
	}

	for _, r := range items {
		published := r.PublishedAt

		tp.AddField(r.Id.String())
		tp.AddField(stringOrDash(r.Title))
		tp.AddField(r.Url)
		tp.AddField(formatLastRun(&published, isTTY))
		tp.EndRow()
	}

	return tp.Render()
}

// --- view ---

func newRecordViewCmd(version string) *cobra.Command {
	viewCmd := &cobra.Command{
		Use:   "view <id>",
		Short: "Show a record",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRecordView(cmd, version, args[0])
		},
	}

	addRepoFlag(viewCmd)
	output.AddJSONFlags(viewCmd, recordViewJSONFields)

	return viewCmd
}

func runRecordView(cmd *cobra.Command, version, idArg string) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	recordID, err := parseRecordID(idArg)
	if err != nil {
		return err
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	resp, err := apiClient.API.GetRecordWithResponse(cmd.Context(), repoID, recordID)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("record %s", recordID)); apiErr != nil {
		return apiErr
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), recordRow(*resp.JSON200), jf.Fields, jf.JQ)
	}

	return renderRecordView(cmd.OutOrStdout(), *resp.JSON200)
}

// renderRecordView prints r the way `record view` and a successful `record
// create`/`record update` (editor or field-flag path alike) all render it:
// title, url, published/created/updated, tags, image url, then the text in
// full (never truncated) — matching the brief's field list exactly.
func renderRecordView(w io.Writer, r api.Record) error {
	fields := []struct{ label, value string }{
		{"Title", stringOrDash(r.Title)},
		{"URL", r.Url},
		{"Published", r.PublishedAt.UTC().Format(time.RFC3339)},
		{"Created", r.CreatedAt.UTC().Format(time.RFC3339)},
		{"Updated", r.UpdatedAt.UTC().Format(time.RFC3339)},
		{"Tags", tagsOrDash(r.Tags)},
		{"Image URL", stringOrDash(r.ImageUrl)},
	}

	for _, f := range fields {
		if _, err := fmt.Fprintf(w, "%s: %s\n", f.label, f.value); err != nil {
			return fmt.Errorf("writing record view: %w", err)
		}
	}

	if _, err := fmt.Fprintln(w, "Text:"); err != nil {
		return fmt.Errorf("writing record view: %w", err)
	}

	if _, err := fmt.Fprintln(w, stringOrDash(r.Text)); err != nil {
		return fmt.Errorf("writing record view: %w", err)
	}

	return nil
}

// --- create ---

func newRecordCreateCmd(version string) *cobra.Command {
	var title, url, text, tagsRaw, published, inputPath string

	createCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a record",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runRecordCreate(cmd, version, recordCreateFlags{
				title:            title,
				titleChanged:     cmd.Flags().Changed("title"),
				url:              url,
				urlChanged:       cmd.Flags().Changed("url"),
				text:             text,
				textChanged:      cmd.Flags().Changed("text"),
				tags:             tagsRaw,
				tagsChanged:      cmd.Flags().Changed("tags"),
				published:        published,
				publishedChanged: cmd.Flags().Changed("published"),
				inputPath:        inputPath,
				inputChanged:     cmd.Flags().Changed("input"),
			})
		},
	}

	addRepoFlag(createCmd)
	output.AddJSONFlags(createCmd, recordViewJSONFields)

	flags := createCmd.Flags()
	flags.StringVar(&title, "title", "", "Title (required unless --input)")
	flags.StringVar(&url, "url", "", "URL (required unless --input)")
	flags.StringVar(&text, "text", "", "Text body")
	flags.StringVar(&tagsRaw, "tags", "", "Comma-separated tags")
	flags.StringVar(&published, "published", "", "Published time, RFC 3339 (required unless --input)")
	flags.StringVar(&inputPath, "input", "",
		"Read a full RecordCreate JSON document from this file, or - for stdin (mutually exclusive with the field flags)")

	return createCmd
}

type recordCreateFlags struct {
	title            string
	titleChanged     bool
	url              string
	urlChanged       bool
	text             string
	textChanged      bool
	tags             string
	tagsChanged      bool
	published        string
	publishedChanged bool
	inputPath        string
	inputChanged     bool
}

func runRecordCreate(cmd *cobra.Command, version string, f recordCreateFlags) error {
	jf, err := output.ReadJSONFlags(cmd)
	if err != nil {
		return err
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	usesFieldFlags := f.titleChanged || f.urlChanged || f.textChanged || f.tagsChanged || f.publishedChanged

	if f.inputChanged && usesFieldFlags {
		return errors.New("--input and the field flags (--title, --url, --text, --tags, --published) are mutually exclusive")
	}

	var body api.RecordCreate

	if f.inputChanged {
		body, err = readRecordCreate(cmd, f.inputPath)
		if err != nil {
			return err
		}
	} else {
		if !f.titleChanged {
			return errors.New("--title is required unless --input is given")
		}
		if !f.urlChanged {
			return errors.New("--url is required unless --input is given")
		}
		if !f.publishedChanged {
			return errors.New("--published is required unless --input is given")
		}

		publishedAt, parseErr := time.Parse(time.RFC3339, f.published)
		if parseErr != nil {
			return fmt.Errorf("invalid --published %q: %w", f.published, parseErr)
		}

		body = api.RecordCreate{
			Title:       f.title,
			Url:         f.url,
			PublishedAt: publishedAt,
		}

		if f.textChanged {
			t := f.text
			body.Text = &t
		}
		if f.tagsChanged {
			tags := splitTags(f.tags)
			body.Tags = &tags
		}
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	resp, err := apiClient.API.CreateRecordWithResponse(cmd.Context(), repoID, body)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, ""); apiErr != nil {
		return apiErr
	}

	if jf.Requested {
		return output.PrintJSONObject(cmd.OutOrStdout(), recordRow(*resp.JSON201), jf.Fields, jf.JQ)
	}

	return renderRecordView(cmd.OutOrStdout(), *resp.JSON201)
}

// readRecordCreate reads --input's value: path's file content, or stdin
// when path is "-", parsed as a full RecordCreate document. Invalid JSON
// fails here — a local error before any request.
func readRecordCreate(cmd *cobra.Command, path string) (api.RecordCreate, error) {
	var data []byte
	var err error

	if path == "-" {
		data, err = io.ReadAll(cmd.InOrStdin())
	} else {
		data, err = os.ReadFile(path)
	}

	if err != nil {
		return api.RecordCreate{}, fmt.Errorf("reading --input %s: %w", path, err)
	}

	var body api.RecordCreate
	if err := json.Unmarshal(data, &body); err != nil {
		return api.RecordCreate{}, fmt.Errorf("invalid --input JSON: %w", err)
	}

	return body, nil
}

// --- update ---

func newRecordUpdateCmd(version string) *cobra.Command {
	var title, url, text, tagsRaw string
	var useEditor bool

	updateCmd := &cobra.Command{
		Use:   "update <id>",
		Short: "Update a record's fields",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRecordUpdate(cmd, version, args[0], recordUpdateFlags{
				title:        title,
				titleChanged: cmd.Flags().Changed("title"),
				url:          url,
				urlChanged:   cmd.Flags().Changed("url"),
				text:         text,
				textChanged:  cmd.Flags().Changed("text"),
				tags:         tagsRaw,
				tagsChanged:  cmd.Flags().Changed("tags"),
				useEditor:    useEditor,
			}, launchSystemEditor)
		},
	}

	addRepoFlag(updateCmd)

	flags := updateCmd.Flags()
	flags.StringVar(&title, "title", "", "New title")
	flags.StringVar(&url, "url", "", "New URL")
	flags.StringVar(&text, "text", "", "New text")
	flags.StringVar(&tagsRaw, "tags", "", "Comma-separated tags, replacing the current set")
	flags.BoolVar(&useEditor, "editor", false,
		"Edit the record's updatable fields interactively in $VISUAL/$EDITOR/vi, kubectl-edit style")

	return updateCmd
}

type recordUpdateFlags struct {
	title        string
	titleChanged bool
	url          string
	urlChanged   bool
	text         string
	textChanged  bool
	tags         string
	tagsChanged  bool
	useEditor    bool
}

// recordFieldOverrides is the subset of RecordUpdate driven by field flags
// (--title/--url/--text/--tags) — shared between the direct-PATCH path
// (runRecordUpdate) and the editor loop (runRecordEditor in
// record_editor.go), mirroring repositoryFieldOverrides/sourceFieldOverrides:
// a field flag always wins over whatever the same field held in the edited
// JSON document, applied after parsing it.
type recordFieldOverrides struct {
	title *string
	url   *string
	text  *string
	tags  *[]string
}

func runRecordUpdate(cmd *cobra.Command, version, idArg string, f recordUpdateFlags, editor editorFunc) error {
	if !f.useEditor && !f.titleChanged && !f.urlChanged && !f.textChanged && !f.tagsChanged {
		return errors.New("nothing to update: give --title, --url, --text, --tags, or --editor")
	}

	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	recordID, err := parseRecordID(idArg)
	if err != nil {
		return err
	}

	overrides := recordFieldOverrides{}
	if f.titleChanged {
		t := f.title
		overrides.title = &t
	}
	if f.urlChanged {
		u := f.url
		overrides.url = &u
	}
	if f.textChanged {
		t := f.text
		overrides.text = &t
	}
	if f.tagsChanged {
		tags := splitTags(f.tags)
		overrides.tags = &tags
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	if f.useEditor {
		return runRecordEditor(cmd, apiClient, repoID, recordID, overrides, editor)
	}

	return applyRecordFieldUpdate(cmd, apiClient, repoID, recordID, overrides)
}

// applyRecordFieldUpdate sends one PATCH built purely from field flags,
// without an If-Match header — "no If-Match: last write wins", mirroring
// repo/source update's non-editor path.
func applyRecordFieldUpdate(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, recordID api.RecordId, overrides recordFieldOverrides,
) error {
	patch := recordOverridesToUpdate(overrides)

	resp, err := apiClient.API.UpdateRecordWithResponse(cmd.Context(), repoID, recordID, nil, patch)
	if err != nil {
		return err
	}
	if apiErr := NewAPIError(resp, fmt.Sprintf("record %s", recordID)); apiErr != nil {
		return apiErr
	}

	return renderRecordView(cmd.OutOrStdout(), *resp.JSON200)
}

func recordOverridesToUpdate(o recordFieldOverrides) api.RecordUpdate {
	return api.RecordUpdate{
		Title: o.title,
		Url:   o.url,
		Text:  o.text,
		Tags:  o.tags,
	}
}

// --- delete ---

func newRecordDeleteCmd(version string) *cobra.Command {
	var yes bool

	deleteCmd := &cobra.Command{
		Use:   "delete <id>...",
		Short: "Delete one or more records",
		Args:  cobra.MinimumNArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runRecordDelete(cmd, version, args, yes)
		},
	}

	addRepoFlag(deleteCmd)
	deleteCmd.Flags().BoolVar(&yes, "yes", false, "Skip the confirmation prompt")

	return deleteCmd
}

func runRecordDelete(cmd *cobra.Command, version string, idArgs []string, yes bool) error {
	repoID, _, err := resolveRepoID(cmd, true)
	if err != nil {
		return err
	}

	// Every id is parsed (and so validated) up front, before any request —
	// "invalid UUIDs are rejected before any request" (brief, requirement 5).
	ids := make([]api.RecordId, len(idArgs))
	for i, a := range idArgs {
		id, parseErr := parseRecordID(a)
		if parseErr != nil {
			return parseErr
		}

		ids[i] = id
	}

	apiClient, err := newAPIClient(cmd, version)
	if err != nil {
		return err
	}

	return deleteRecords(cmd, apiClient, repoID, ids, stdinIsTTY(cmd.InOrStdin()), yes)
}

// deleteRecords confirms once for the whole batch (via confirmDelete — no
// GET needed to name anything, unlike repo delete's single-resource
// prompt), then DELETEs each id in order, printing one "deleted <id>" line
// per successful id to stdout (cmd.OutOrStdout()) or one "error: <id>:
// <message>" line per failed id to stderr (cmd.ErrOrStderr()) — stdout
// stays data-only, so a script capturing it to learn what was deleted
// never gets error text mixed in — and continuing after a failure. Returns
// a non-nil error (exit 1) if any id failed, nil (exit 0) otherwise.
//
// Kept independent of cobra flag parsing and of
// client.NewFromConfig/config.Load, taking isTTY as an explicit parameter,
// so it can be exercised directly against an httptest server with an
// injected isTTY/yes/stdin combination — see record_test.go.
func deleteRecords(cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, ids []api.RecordId, isTTY, yes bool) error {
	prompt := fmt.Sprintf("Delete %d records from %s?", len(ids), repoID)
	if err := confirmDelete(cmd, isTTY, yes, prompt); err != nil {
		return err
	}

	failed := false

	for _, id := range ids {
		resp, delErr := apiClient.API.DeleteRecordWithResponse(cmd.Context(), repoID, id)
		if delErr != nil {
			failed = true

			_, _ = fmt.Fprintf(cmd.ErrOrStderr(), "error: %s: %s\n", id, delErr)

			continue
		}

		if apiErr := NewAPIError(resp, fmt.Sprintf("record %s", id)); apiErr != nil {
			failed = true

			_, _ = fmt.Fprintf(cmd.ErrOrStderr(), "error: %s: %s\n", id, apiErr)

			continue
		}

		_, _ = fmt.Fprintf(cmd.OutOrStdout(), "deleted %s\n", id)
	}

	if failed {
		return &recordDeleteFailedError{}
	}

	return nil
}

// recordDeleteFailedError signals a batch delete's exit code (1) without
// re-printing anything to stderr: deleteRecords already printed one
// "deleted <id>" line (stdout) or "error: <id>: <message>" line (stderr)
// per id as it went, so main's default "error: <message>\n" rendering
// would be a redundant, less specific summary line — RenderError is a
// deliberate no-op.
type recordDeleteFailedError struct{}

func (e *recordDeleteFailedError) Error() string { return "one or more records failed to delete" }

func (e *recordDeleteFailedError) RenderError(io.Writer) {}

func (e *recordDeleteFailedError) ExitCode() int { return 1 }

// --- shared helpers ---

func parseRecordID(idArg string) (api.RecordId, error) {
	id, err := uuid.Parse(idArg)
	if err != nil {
		return api.RecordId{}, fmt.Errorf("invalid record id %q: %w", idArg, err)
	}

	return id, nil
}

// recordRow builds a record's --json Row, covering every field in
// recordViewJSONFields (a superset of recordListJSONFields — narrow()
// drops whatever the caller didn't ask for).
func recordRow(r api.Record) output.Row {
	return output.Row{
		"id":          r.Id.String(),
		"url":         r.Url,
		"title":       stringOrNil(r.Title),
		"text":        stringOrNil(r.Text),
		"html":        stringOrNil(r.Html),
		"imageUrl":    stringOrNil(r.ImageUrl),
		"createdAt":   r.CreatedAt.UTC().Format(time.RFC3339),
		"publishedAt": r.PublishedAt.UTC().Format(time.RFC3339),
		"updatedAt":   r.UpdatedAt.UTC().Format(time.RFC3339),
		"startingAt":  timeOrNil(r.StartingAt),
		"tags":        tagsOrNil(r.Tags),
	}
}
