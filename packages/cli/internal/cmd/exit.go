package cmd

import "fmt"

// ExitCancelled is exit code 2 — cancelled by the user (SIGINT, or an
// editor session aborted). main.go's run() builds NewExitError(ExitCancelled,
// …) itself when the command's context was cancelled by a SIGINT; a command
// like C4's interactive editor flow builds the same code directly for a
// non-SIGINT cancellation (an aborted edit).
const ExitCancelled = 2

// ExitAuthRequired is exit code 4 — authentication required. It's exposed
// here as the canonical value behind config.NotLoggedInError.ExitCode and
// APIError.ExitCode (a 401 response), so any future error type that also
// means "not authenticated" uses the same constant instead of a repeated
// literal 4.
const ExitAuthRequired = 4

// ExitError is an error that carries the process exit code feedctl should
// use when it reaches main. cmd/feedctl/main.go unwraps it with errors.As;
// an error that isn't one exits 1.
type ExitError struct {
	err  error
	code int
}

// NewExitError builds an ExitError with the given exit code and a
// fmt.Errorf-formatted message.
func NewExitError(code int, format string, args ...any) *ExitError {
	return &ExitError{err: fmt.Errorf(format, args...), code: code}
}

func (e *ExitError) Error() string { return e.err.Error() }

func (e *ExitError) Unwrap() error { return e.err }

// ExitCode returns the process exit code this error should produce.
func (e *ExitError) ExitCode() int { return e.code }
