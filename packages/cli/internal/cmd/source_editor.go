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

// sourceEditorNouns is `source update --editor`'s editorNouns: the editable
// document is the flow, embedded within the wider SourceUpdate the field
// overrides (--title/--tags/--disabled) also write into.
func sourceEditorNouns(sourceID api.SourceId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("the flow for source %s", sourceID),
		Noun:    "flow",
	}
}

// runFlowEditor is the `source update --editor` loop (requirement 3 of the
// C4 brief): GET the source and its ETag, let the user edit its flow as
// JSON (the generalized runEditorLoop owns the temp-file/reopen/cancel
// mechanics; this function supplies the source-specific GET/PATCH glue),
// and PATCH the result. overrides carries --title/--tags/--disabled, folded
// into every PATCH attempt alongside the edited flow ("other flags may be
// combined with --editor; they go into the same PATCH").
//
// It is kept as its own small unit, independent of cobra flag parsing and
// of client.NewFromConfig/config.Load, so it can be exercised directly
// against an httptest server with a fake editorFunc — see
// source_editor_test.go.
func runFlowEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	overrides sourceFieldOverrides, editor editorFunc,
) error {
	nouns := sourceEditorNouns(sourceID)

	var etag string

	// fetch is shared by the initial GET (cfg.get) and attempt's own
	// re-fetch on a 412 — both need to keep etag (captured by this
	// closure) current for the next PATCH attempt.
	fetch := func(ctx context.Context) (string, error) {
		resp, err := apiClient.API.GetSourceWithResponse(ctx, repoID, sourceID)
		if err != nil {
			return "", err
		}
		if apiErr := NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
			return "", apiErr
		}

		etag = etagOf(resp.Headers200)

		return prettyFlow(resp.JSON200.Flow), nil
	}

	cfg := editorLoopConfig{
		tempFilePattern: fmt.Sprintf("feedctl-source-%s-*.json", sourceID),
		nouns:           nouns,
		get:             fetch,
		validate: func(editedBody string) error {
			var flow api.ScrapeFlow
			return json.Unmarshal([]byte(editedBody), &flow)
		},
		attempt: func(ctx context.Context, editedBody string) (editorAttemptResult, error) {
			var flow api.ScrapeFlow
			_ = json.Unmarshal([]byte(editedBody), &flow) // already validated by cfg.validate

			patch := api.SourceUpdate{Title: overrides.title, Tags: overrides.tags, Disabled: overrides.disabled, Flow: &flow}

			var ifMatch *api.IfMatch
			if etag != "" {
				e := etag
				ifMatch = &e
			}

			updResp, patchErr := apiClient.API.UpdateSourceWithResponse(ctx, repoID, sourceID, &api.UpdateSourceParams{IfMatch: ifMatch}, patch)
			if patchErr != nil {
				return editorAttemptResult{}, patchErr
			}

			switch updResp.StatusCode() {
			case http.StatusOK:
				renderErr := renderSourceView(cmd.OutOrStdout(), *updResp.JSON200)

				return editorAttemptResult{Done: true}, renderErr

			case http.StatusBadRequest:
				var serverErr api.ApiError
				_ = json.Unmarshal(updResp.Body, &serverErr)

				return editorAttemptResult{Comment: serverErrorComment(nouns, serverErr.Message, fieldErrorLines(serverErr)), Body: editedBody}, nil

			case http.StatusPreconditionFailed:
				fresh, fetchErr := fetch(ctx)
				if fetchErr != nil {
					return editorAttemptResult{}, fetchErr
				}

				return editorAttemptResult{Comment: conflictComment(nouns, editedBody), Body: fresh}, nil

			default:
				return editorAttemptResult{}, NewAPIError(updResp, fmt.Sprintf("source %s", sourceID))
			}
		},
	}

	return runEditorLoop(cmd, cfg, editor)
}

// fieldErrorLines renders an ApiError's field errors (VALIDATION_ERROR
// responses) as the "  <field>: <message>" lines serverErrorComment
// appends after its headline — the same format APIError.RenderError uses.
func fieldErrorLines(apiErr api.ApiError) []string {
	if apiErr.Errors == nil {
		return nil
	}

	lines := make([]string, 0, len(*apiErr.Errors))
	for _, fe := range *apiErr.Errors {
		lines = append(lines, fmt.Sprintf("  %s: %s", fe.Field, fe.Message))
	}

	return lines
}

func etagOf(headers *api.GetSourceResponse200Headers) string {
	if headers == nil || headers.ETag == nil {
		return ""
	}

	return *headers.ETag
}
