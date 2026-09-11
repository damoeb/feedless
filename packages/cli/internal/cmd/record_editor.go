package cmd

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
)

// recordEditorNouns is `record update --editor`'s editorNouns.
func recordEditorNouns(recordID api.RecordId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("record %s", recordID),
		Noun:    "record",
	}
}

// recordToUpdate maps a GET response's Record onto the RecordUpdate shape
// `record update --editor` presents for editing: title, text, url, tags —
// the exact fields RecordUpdate declares (openapi.yaml). publishedAt and
// every other Record field are not part of RecordUpdate at all, so they're
// simply absent from the editable document — there's nothing to omit or
// clear, the server-side schema never accepts them on a PATCH.
//
// title/text/tags are carried over as-is from r (already *string/*[]string,
// nil when GET didn't return them) so a field the user never touches, or
// that GET never returned in the first place, marshals as an absent JSON
// key (the generated RecordUpdate fields are all `omitempty`) rather than
// an explicit null or empty-string/empty-array — never clearing data the
// user didn't intend to touch. url is always present on Record (a required,
// non-pointer field), so it's always prefilled.
func recordToUpdate(r api.Record) api.RecordUpdate {
	url := r.Url

	return api.RecordUpdate{
		Title: r.Title,
		Text:  r.Text,
		Url:   &url,
		Tags:  r.Tags,
	}
}

// runRecordEditor is `record update --editor`'s loop: GET the record and
// its ETag, let the user edit its updatable fields (title, text, url, tags)
// as JSON via the generalized runEditorLoop (editor.go), and PATCH the
// result. overrides carries --title/--url/--text/--tags, applied on top of
// the edited document after it's parsed — a field flag always wins over
// whatever the same field held in the JSON, mirroring
// runRepositoryEditor/runFlowEditor's own overrides.
//
// Kept as its own small unit, independent of cobra flag parsing and of
// client.NewFromConfig/config.Load, exercised directly against an httptest
// server with a fake editorFunc — see record_editor_test.go.
func runRecordEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, recordID api.RecordId,
	overrides recordFieldOverrides, editor editorFunc,
) error {
	nouns := recordEditorNouns(recordID)

	var etag string

	// fetch is shared by the initial GET (cfg.get) and attempt's own
	// re-fetch on a 412 — both need to keep etag (captured by this closure)
	// current for the next PATCH attempt.
	fetch := func(ctx context.Context) (string, error) {
		resp, err := apiClient.API.GetRecordWithResponse(ctx, repoID, recordID)
		if err != nil {
			return "", err
		}
		if apiErr := NewAPIError(resp, fmt.Sprintf("record %s", recordID)); apiErr != nil {
			return "", apiErr
		}

		etag = etagOfRecord(resp.Headers200)

		return prettyJSON(recordToUpdate(*resp.JSON200)), nil
	}

	cfg := editorLoopConfig{
		tempFilePattern: fmt.Sprintf("feedctl-record-%s-*.json", recordID),
		nouns:           nouns,
		get:             fetch,
		validate: func(editedBody string) error {
			var update api.RecordUpdate

			return json.Unmarshal([]byte(editedBody), &update)
		},
		attempt: func(ctx context.Context, editedBody string) (editorAttemptResult, error) {
			var update api.RecordUpdate
			_ = json.Unmarshal([]byte(editedBody), &update) // already validated by cfg.validate

			applyRecordOverrides(&update, overrides)

			var ifMatch *api.IfMatch
			if etag != "" {
				e := etag
				ifMatch = &e
			}

			updResp, patchErr := apiClient.API.UpdateRecordWithResponse(ctx, repoID, recordID, &api.UpdateRecordParams{IfMatch: ifMatch}, update)
			if patchErr != nil {
				return editorAttemptResult{}, patchErr
			}

			switch updResp.StatusCode() {
			case http.StatusOK:
				renderErr := renderRecordView(cmd.OutOrStdout(), *updResp.JSON200)

				return editorAttemptResult{Done: true}, renderErr

			case http.StatusBadRequest:
				var serverErr api.ApiError
				_ = json.Unmarshal(updResp.Body, &serverErr)

				return editorAttemptResult{
					Comment: serverErrorComment(nouns, serverErr.Message, fieldErrorLines(serverErr)),
					Body:    editedBody,
				}, nil

			case http.StatusPreconditionFailed:
				fresh, fetchErr := fetch(ctx)
				if fetchErr != nil {
					return editorAttemptResult{}, fetchErr
				}

				return editorAttemptResult{Comment: conflictComment(nouns, editedBody), Body: fresh}, nil

			default:
				return editorAttemptResult{}, NewAPIError(updResp, fmt.Sprintf("record %s", recordID))
			}
		},
	}

	return runEditorLoop(cmd, cfg, editor)
}

// applyRecordOverrides folds --title/--url/--text/--tags into update in
// place, after it's been parsed from the edited JSON.
func applyRecordOverrides(update *api.RecordUpdate, overrides recordFieldOverrides) {
	if overrides.title != nil {
		update.Title = overrides.title
	}
	if overrides.url != nil {
		update.Url = overrides.url
	}
	if overrides.text != nil {
		update.Text = overrides.text
	}
	if overrides.tags != nil {
		update.Tags = overrides.tags
	}
}

func etagOfRecord(headers *api.GetRecordResponse200Headers) string {
	if headers == nil || headers.ETag == nil {
		return ""
	}

	return *headers.ETag
}
