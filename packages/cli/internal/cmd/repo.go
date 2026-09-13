package cmd

import (
	"errors"
	"fmt"
	"os"

	"github.com/google/uuid"
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// EnvRepo mirrors gh's GH_REPO.
const EnvRepo = "FEEDCTL_REPO"

func addRepoFlag(cmd *cobra.Command) {
	cmd.Flags().StringP("repo", "R", "", "Repository to operate on (or set FEEDCTL_REPO)")
}

// With required false, "" means no repository scope (source list across all repositories).
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

// present only matters when required is false.
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

// -S has no environment fallback, unlike -R.
func addSourceFlag(cmd *cobra.Command) {
	cmd.Flags().StringP("source", "S", "", "Source to operate on (required)")
}

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
