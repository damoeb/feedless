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

// The whole RepositoryUpdate is editable, unlike source where only the flow is.
func repositoryEditorNouns(repoID api.RepositoryId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("repository %s", repoID),
		Noun:    "repository",
	}
}

// GET never returns retention or pushNotificationsMuted, so they start unset; the user can still add them.
func repositoryToUpdate(r api.Repository) api.RepositoryUpdate {
	title := r.Title
	description := r.Description
	cron := r.RefreshCron
	visibility := r.Visibility

	return api.RepositoryUpdate{
		Title:       &title,
		Description: &description,
		RefreshCron: &cron,
		Visibility:  &visibility,
	}
}

func runRepositoryEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId,
	overrides repositoryFieldOverrides, editor editorFunc,
) error {
	nouns := repositoryEditorNouns(repoID)

	var etag string

	// Shared by the initial GET and the 412 re-fetch, so the etag stays current.
	fetch := func(ctx context.Context) (string, error) {
		resp, err := apiClient.API.GetRepositoryWithResponse(ctx, repoID)
		if err != nil {
			return "", err
		}
		if apiErr := NewAPIError(resp, fmt.Sprintf("repository %s", repoID)); apiErr != nil {
			return "", apiErr
		}

		etag = etagOfRepository(resp.Headers200)

		return prettyJSON(repositoryToUpdate(*resp.JSON200)), nil
	}

	cfg := editorLoopConfig{
		tempFilePattern: fmt.Sprintf("feedctl-repo-%s-*.json", repoID),
		nouns:           nouns,
		get:             fetch,
		validate: func(editedBody string) error {
			var update api.RepositoryUpdate

			return json.Unmarshal([]byte(editedBody), &update)
		},
		attempt: func(ctx context.Context, editedBody string) (editorAttemptResult, error) {
			var update api.RepositoryUpdate
			_ = json.Unmarshal([]byte(editedBody), &update) // already validated by cfg.validate

			applyRepositoryOverrides(&update, overrides)

			var ifMatch *api.IfMatch
			if etag != "" {
				e := etag
				ifMatch = &e
			}

			updResp, patchErr := apiClient.API.UpdateRepositoryWithResponse(ctx, repoID, &api.UpdateRepositoryParams{IfMatch: ifMatch}, update)
			if patchErr != nil {
				return editorAttemptResult{}, patchErr
			}

			switch updResp.StatusCode() {
			case http.StatusOK:
				renderErr := renderRepositoryView(cmd.OutOrStdout(), *updResp.JSON200)

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
				return editorAttemptResult{}, NewAPIError(updResp, fmt.Sprintf("repository %s", repoID))
			}
		},
	}

	return runEditorLoop(cmd, cfg, editor)
}

func applyRepositoryOverrides(update *api.RepositoryUpdate, overrides repositoryFieldOverrides) {
	if overrides.title != nil {
		update.Title = overrides.title
	}
	if overrides.description != nil {
		update.Description = overrides.description
	}
	if overrides.refreshCron != nil {
		update.RefreshCron = overrides.refreshCron
	}
	if overrides.visibility != nil {
		update.Visibility = overrides.visibility
	}
}

func etagOfRepository(headers *api.GetRepositoryResponse200Headers) string {
	if headers == nil || headers.ETag == nil {
		return ""
	}

	return *headers.ETag
}
