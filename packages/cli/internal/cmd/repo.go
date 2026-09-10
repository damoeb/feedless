package cmd

import (
	"errors"
	"fmt"
	"os"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// EnvRepo is the environment variable -R/--repo falls back to when not
// passed on the command line, mirroring gh's -R/GH_REPO.
const EnvRepo = "FEEDCTL_REPO"

// addRepoFlag registers -R/--repo on cmd. Every command that scopes its
// work to one repository calls this and then resolveRepo or resolveRepoID
// to read the value back — source list/view/update here, and C5's source
// run and harvest commands.
func addRepoFlag(cmd *cobra.Command) {
	cmd.Flags().StringP("repo", "R", "", "Repository to operate on (or set FEEDCTL_REPO)")
}

// resolveRepo reads -R/--repo off cmd, falling back to FEEDCTL_REPO — the
// resolution order named in the brief ("the parent repository is -R/--repo,
// defaulting from FEEDCTL_REPO, like gh's -R/GH_REPO"). When required is
// true and neither is set, it fails with the exact message the brief
// specifies ("-R/--repo is required") — a plain error, so it exits 1 (the
// default), not one of feedctl's named exit codes. When required is false,
// an empty result ("", nil) means "operate without a repository scope"
// (source list's fallback from /repositories/{r}/sources to /user/sources).
func resolveRepo(cmd *cobra.Command, required bool) (string, error) {
	repo, err := cmd.Flags().GetString("repo")
	if err != nil {
		return "", fmt.Errorf("reading --repo: %w", err)
	}

	if repo == "" {
		repo = os.Getenv(EnvRepo)
	}

	if repo == "" && required {
		return "", errors.New("-R/--repo is required")
	}

	return repo, nil
}

// resolveRepoID is resolveRepo plus UUID parsing — repositoryId is a UUID
// on every endpoint that takes one (openapi.yaml's RepositoryId path
// parameter). present reports whether a repository was resolved at all
// (only meaningful when required is false; when required is true, a
// non-nil error already means "no repo" and present is meaningless). id is
// the zero UUID when present is false.
func resolveRepoID(cmd *cobra.Command, required bool) (id api.RepositoryId, present bool, err error) {
	repo, err := resolveRepo(cmd, required)
	if err != nil {
		return api.RepositoryId{}, false, err
	}

	if repo == "" {
		return api.RepositoryId{}, false, nil
	}

	id, err = uuid.Parse(repo)
	if err != nil {
		return api.RepositoryId{}, false, fmt.Errorf("invalid -R/--repo %q: %w", repo, err)
	}

	return id, true, nil
}

// addSourceFlag registers -S/--source on cmd, analogous to addRepoFlag
// above. The harvest commands (C5) scope their work to one source's
// harvests and always require it — unlike -R/--repo, -S has no environment
// variable fallback (the brief doesn't specify one).
func addSourceFlag(cmd *cobra.Command) {
	cmd.Flags().StringP("source", "S", "", "Source to operate on (required)")
}

// resolveSourceID reads -S/--source off cmd and parses it as a UUID,
// failing with the exact "-S/--source is required" message when it's
// empty — mirroring resolveRepoID's required path, but always required (the
// harvest commands never operate without a source).
func resolveSourceID(cmd *cobra.Command) (api.SourceId, error) {
	source, err := cmd.Flags().GetString("source")
	if err != nil {
		return api.SourceId{}, fmt.Errorf("reading --source: %w", err)
	}

	if source == "" {
		return api.SourceId{}, errors.New("-S/--source is required")
	}

	id, err := uuid.Parse(source)
	if err != nil {
		return api.SourceId{}, fmt.Errorf("invalid -S/--source %q: %w", source, err)
	}

	return id, nil
}
