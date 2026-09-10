// Package cmd builds the feedctl command tree.
//
// NewRootCmd is the single entry point cmd/feedctl/main.go calls. Later
// tasks (C2-C5) add auth, api, source, and harvest subcommands here — each
// in its own file, registered with root.AddCommand in NewRootCmd.
package cmd

import "github.com/spf13/cobra"

// NewRootCmd builds the feedctl root command. version is embedded by main
// via `-ldflags "-X main.version=..."` and printed by `feedctl --version`.
func NewRootCmd(version string) *cobra.Command {
	root := &cobra.Command{
		Use:           "feedctl",
		Short:         "feedctl is a gh-style CLI for the Feedless HTTP API",
		SilenceUsage:  true,
		SilenceErrors: true,
	}

	root.Version = version
	root.SetVersionTemplate("feedctl version {{.Version}}\n")

	return root
}
