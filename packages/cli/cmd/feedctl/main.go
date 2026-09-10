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
// root command configures it. An error exits 1, unless it carries a
// *cmd.ExitError (errors.As), whose code is used instead — e.g. auth
// commands exit 4 when not logged in or a token is rejected. C3 owns
// richer error rendering (ApiError bodies) on top of this.
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

	var exitErr *cmd.ExitError
	if errors.As(err, &exitErr) {
		return exitErr.ExitCode()
	}

	return 1
}
