package cmd

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// APIError is the one mapping from a non-2xx API response to feedctl's message and exit code.
type APIError struct {
	Status int
	Body   api.ApiError
	// What names the resource in the 404 message, e.g. "source <id>".
	What string
}

// apiResponse is satisfied by every generated …Response type.
type apiResponse interface {
	StatusCode() int
	GetBody() []byte
}

// NewAPIError returns nil for a 2xx response, so callers can call it unconditionally.
func NewAPIError(resp apiResponse, what string) error {
	status := resp.StatusCode()
	if status >= http.StatusOK && status < http.StatusMultipleChoices {
		return nil
	}

	var body api.ApiError

	_ = json.Unmarshal(resp.GetBody(), &body)

	return &APIError{Status: status, Body: body, What: what}
}

// Error sanitizes the server's message here, the one place every rendering goes through.
func (e *APIError) Error() string {
	if e.Status == http.StatusNotFound {
		return "not found: " + e.what()
	}

	if e.Body.Message != "" {
		return output.SafeText(e.Body.Message)
	}

	return http.StatusText(e.Status)
}

func (e *APIError) what() string {
	if e.What != "" {
		return e.What
	}

	return "resource"
}

// ExitCode is 4 when unauthenticated, like other auth failures; 1 otherwise, including 404.
func (e *APIError) ExitCode() int {
	if e.Status == http.StatusUnauthorized {
		return ExitAuthRequired
	}

	return 1
}

// Field names and messages are server-provided (they echo input), so both are sanitized.
func (e *APIError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintf(w, "error: %s\n", e.Error())

	if e.Body.Errors == nil {
		return
	}

	for _, fe := range *e.Body.Errors {
		_, _ = fmt.Fprintf(w, "  %s: %s\n", output.SafeText(fe.Field), output.SafeText(fe.Message))
	}
}
