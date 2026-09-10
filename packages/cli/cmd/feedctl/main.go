// Command feedctl is a gh-style CLI over the Feedless HTTP API (/api/v1).
package main

import (
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
// bodies, exit codes) on top of this.
func run(args []string, stdout, stderr io.Writer) int {
	root := cmd.NewRootCmd(version)
	root.SetArgs(args)
	root.SetOut(stdout)
	root.SetErr(stderr)

	if err := root.Execute(); err != nil {
		_, _ = fmt.Fprintf(stderr, "error: %s\n", err)
		return 1
	}

	return 0
}
