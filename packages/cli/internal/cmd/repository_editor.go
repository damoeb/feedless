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

// repositoryEditorNouns is `repo update --editor`'s editorNouns: unlike
// source (where the editable document is just the flow field, embedded
// within a wider SourceUpdate), the whole updatable document IS the
// RepositoryUpdate shape here.
func repositoryEditorNouns(repoID api.RepositoryId) editorNouns {
	return editorNouns{
		Subject: fmt.Sprintf("repository %s", repoID),
		Noun:    "repository",
	}
}

// repositoryToUpdate maps a GET response's Repository onto the
// RepositoryUpdate shape `repo update --editor` presents for editing.
//
// Only the fields both schemas share — title, description, refreshCron,
// visibility — can be prefilled from what GET returns. retention and
// pushNotificationsMuted are never returned by GET /repositories/{id}
// (openapi.yaml's Repository schema has no such properties; only
// RepositoryCreate/RepositoryUpdate do), so they're left unset in the
// initial document — the editor's baseline simply omits them. The user can
// still add either field to the JSON before saving; the server applies
// whatever the PATCH body sends, same as any other RepositoryUpdate.
// Flagged in the task report as a server-side (http-api) schema gap, out of
// this task's scope to fix.
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

// runRepositoryEditor is `repo update --editor`'s loop: GET the repository
// and its ETag, let the user edit its updatable fields (title, description,
// refreshCron, visibility, retention, pushNotificationsMuted) as JSON via
// the generalized runEditorLoop (editor.go), and PATCH the result.
// overrides carries --title/--description/--cron/--visibility, applied on
// top of the edited document after it's parsed — a field flag always wins
// over whatever the same field held in the JSON, mirroring source update
// --editor's own overrides (sourceFieldOverrides).
//
// Kept as its own small unit, independent of cobra flag parsing and of
// client.NewFromConfig/config.Load, exercised directly against an httptest
// server with a fake editorFunc — see repository_editor_test.go.
func runRepositoryEditor(
	cmd *cobra.Command, apiClient *client.Client, repoID api.RepositoryId,
	overrides repositoryFieldOverrides, editor editorFunc,
) error {
	nouns := repositoryEditorNouns(repoID)

	var etag string

	// fetch is shared by the initial GET (cfg.get) and attempt's own
	// re-fetch on a 412 — both need to keep etag (captured by this
	// closure) current for the next PATCH attempt.
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

// applyRepositoryOverrides folds --title/--description/--cron/--visibility
// into update in place, after it's been parsed from the edited JSON.
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
