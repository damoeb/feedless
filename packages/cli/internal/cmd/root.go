// Package cmd builds the feedctl command tree.
package cmd

import (
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/output"
)

func NewRootCmd(version string) *cobra.Command {
	root := &cobra.Command{
		Use:           "feedctl",
		Short:         "feedctl is a gh-style CLI for the Feedless HTTP API",
		SilenceUsage:  true,
		SilenceErrors: true,
	}

	root.Version = version
	root.SetVersionTemplate("feedctl version {{.Version}}\n")

	root.PersistentFlags().String("host", "", "Feedless host to use (overrides FEEDCTL_HOST and the configured default host)")

	// Registered on the root, so a bare --json on any command lists its fields instead of pflag's generic error.
	root.SetFlagErrorFunc(output.JSONFlagErrorFunc)

	root.AddCommand(newAuthCmd(version))
	root.AddCommand(newAPICmd(version))
	root.AddCommand(newRepositoryCmd(version))
	root.AddCommand(newRecordCmd(version))
	root.AddCommand(newSourceCmd(version))
	root.AddCommand(newHarvestCmd(version))
	root.AddCommand(newStatusCmd(version))

	return root
}
