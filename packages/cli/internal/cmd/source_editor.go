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

// The editable document is the flow; field overrides go into the same SourceUpdate.
func sourceEditorNouns(sourceID api.SourceId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("the flow for source %s", sourceID),
		Noun:    "flow",
	}
}

func runFlowEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId, sourceID api.SourceId,
	overrides sourceFieldOverrides, editor editorFunc,
) error {
	nouns := sourceEditorNouns(sourceID)

	var etag string

	// Shared by the initial GET and the 412 re-fetch, so the etag stays current.
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

// Same format as APIError.RenderError.
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
