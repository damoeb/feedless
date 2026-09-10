package cmd

import "fmt"

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
