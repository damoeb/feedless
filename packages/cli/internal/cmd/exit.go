package cmd

import "fmt"

// ExitCancelled is for SIGINT or an aborted edit.
const ExitCancelled = 2

// ExitAuthRequired is the one "not authenticated" code, shared by every such error.
const ExitAuthRequired = 4

// ExitError carries the exit code main uses; other errors exit 1.
type ExitError struct {
	err  error
	code int
}

func NewExitError(code int, format string, args ...any) *ExitError {
	return &ExitError{err: fmt.Errorf(format, args...), code: code}
}

func (e *ExitError) Error() string { return e.err.Error() }

func (e *ExitError) Unwrap() error { return e.err }

func (e *ExitError) ExitCode() int { return e.code }
