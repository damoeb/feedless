// Command feedctl is a gh-style CLI over the Feedless HTTP API (/api/v1).
package main

import (
	"context"
	"errors"
	"fmt"
	"io"
	"os"
	"os/signal"

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
// root command configures it.
//
// It builds a context cancelled on SIGINT and runs the command against it
// (cmd.Context() in every RunE, and so every generated API call, which
// builds its *http.Request with this context) — cancelling an in-flight
// request rather than leaving it to finish. When that cancellation is why
// the command returned an error, run reports exit code 2
// (cmd.ExitCancelled) instead of whatever the underlying error (a wrapped
// context.Canceled, surfacing as some transport failure) would otherwise
// map to.
func run(args []string, stdout, stderr io.Writer) int {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
	defer stop()

	return runWithContext(ctx, args, stdout, stderr)
}

// runWithContext is run's logic against a caller-supplied context, split
// out so a test can drive the SIGINT-cancellation path (ctx already
// Done/Canceled before Execute) without sending the process a real signal.
func runWithContext(ctx context.Context, args []string, stdout, stderr io.Writer) int {
	root := cmd.NewRootCmd(version)
	root.SetArgs(args)
	root.SetOut(stdout)
	root.SetErr(stderr)

	err := root.ExecuteContext(ctx)
	if err == nil {
		return 0
	}

	if errors.Is(ctx.Err(), context.Canceled) {
		err = cmd.NewExitError(cmd.ExitCancelled, "cancelled")
	}

	renderError(stderr, err)

	return exitCodeFor(err)
}

// errorRenderer lets an error take over printing itself to stderr, beyond
// the generic "error: <message>\n" line — *cmd.APIError implements it to
// also print one indented line per field error (requirement 3's API-error
// rendering). This is the one extension point run's error handling grows
// through; a command's error either implements this (or wraps something
// that does) or falls back to the generic line, there's no second path.
type errorRenderer interface {
	RenderError(w io.Writer)
}

func renderError(w io.Writer, err error) {
	var r errorRenderer
	if errors.As(err, &r) {
		r.RenderError(w)
		return
	}

	_, _ = fmt.Fprintf(w, "error: %s\n", err)
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
