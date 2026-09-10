// Command feedctl is a gh-style CLI over the Feedless HTTP API (/api/v1).
package main

import (
	"errors"
	"fmt"
	"io"
	"os"

	"github.com/damoeb/feedless/packages/cli/internal/cmd"
)

// version is set at build time via -ldflags "-X main.version=<version>"
// (see the Gradle `build` task in packages/cli/build.gradle.kts). It stays
// "dev" for a plain `go build`/`go run`.
var version = "dev"

func main() {
	os.Exit(run(os.Args[1:], os.Stdout, os.Stderr))
}

// run executes the root command against args, writing normal output to
// stdout and errors to stderr, and returns the process exit code. It is
// split out from main so tests can exercise it without a subprocess.
//
// The root command sets SilenceErrors, so cobra never prints the error
// itself — this is the one place that happens. SilenceUsage stays as the
// root command configures it. C3 owns richer error rendering (ApiError
// bodies) on top of this.
func run(args []string, stdout, stderr io.Writer) int {
	root := cmd.NewRootCmd(version)
	root.SetArgs(args)
	root.SetOut(stdout)
	root.SetErr(stderr)

	err := root.Execute()
	if err == nil {
		return 0
	}

	_, _ = fmt.Fprintf(stderr, "error: %s\n", err)

	return exitCodeFor(err)
}

// exitCoder is implemented by every error that carries the process exit
// code feedctl should use — *cmd.ExitError (built directly by commands
// like auth login/status) and *config.NotLoggedInError (returned by
// config.Resolve and, through it, client.NewFromConfig — the entry point
// C3-C5 commands use to get an authenticated client). run() only needs to
// recognize this one method, not either concrete type, so any command
// built on either error keeps working without main.go changing again.
type exitCoder interface {
	ExitCode() int
}

// exitCodeFor decides run()'s exit code for a non-nil error: the code from
// an ExitCode() int method on err or anything it wraps (errors.As), else
// the default of 1.
func exitCodeFor(err error) int {
	var ec exitCoder
	if errors.As(err, &ec) {
		return ec.ExitCode()
	}

	return 1
}
