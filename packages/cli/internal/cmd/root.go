// Package cmd builds the feedctl command tree.
//
// NewRootCmd is the single entry point cmd/feedctl/main.go calls. Later
// tasks (C2-C5) add auth, api, source, and harvest subcommands here — each
// in its own file, registered with root.AddCommand in NewRootCmd.
package cmd

import (
	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// NewRootCmd builds the feedctl root command with the full auth/api/source/
// harvest command tree. version is embedded by main
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

	root.PersistentFlags().String("host", "", "Feedless host to use (overrides FEEDCTL_HOST and the configured default host)")

	// Registered once, here, for the whole command tree: cobra walks up to
	// the root's FlagErrorFunc when a subcommand doesn't set its own (see
	// Command.FlagErrorFunc). It turns a bare --json on any command that
	// calls output.AddJSONFlags into a output.FieldsError listing that
	// command's fields, instead of pflag's generic "flag needs an
	// argument" — see output.JSONFlagErrorFunc.
	root.SetFlagErrorFunc(output.JSONFlagErrorFunc)

	root.AddCommand(newAuthCmd(version))
	root.AddCommand(newAPICmd(version))
	root.AddCommand(newSourceCmd(version))
	root.AddCommand(newHarvestCmd(version))

	return root
}
