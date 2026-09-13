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

// Set via -ldflags "-X main.version=<version>" (see build.gradle.kts).
var version = "dev"

func main() {
	os.Exit(run(os.Args[1:], os.Stdout, os.Stderr))
}

// run is split out from main so tests can call it. SIGINT cancels in-flight requests and exits 2.
func run(args []string, stdout, stderr io.Writer) int {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
	defer stop()

	return runWithContext(ctx, args, stdout, stderr)
}

// runWithContext lets tests drive the SIGINT path without a real signal.
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

// errorRenderer lets an error print more than the generic "error: <message>" line.
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

// exitCoder lets any error carry its exit code without main knowing the concrete type.
type exitCoder interface {
	ExitCode() int
}

func exitCodeFor(err error) int {
	var ec exitCoder
	if errors.As(err, &ec) {
		return ec.ExitCode()
	}

	return 1
}
