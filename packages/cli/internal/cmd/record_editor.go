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

func recordEditorNouns(recordID api.RecordId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("record %s", recordID),
		Noun:    "record",
	}
}

// Only fields GET returned are set, so omitempty keeps untouched fields out of the PATCH.
func recordToUpdate(r api.Record) api.RecordUpdate {
	url := r.Url

	return api.RecordUpdate{
		Title: r.Title,
		Text:  r.Text,
		Url:   &url,
		Tags:  r.Tags,
	}
}

func runRecordEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, recordID api.RecordId,
	overrides recordFieldOverrides, editor editorFunc,
) error {
	nouns := recordEditorNouns(recordID)

	var etag string

	// Shared by the initial GET and the 412 re-fetch, so the etag stays current.
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
