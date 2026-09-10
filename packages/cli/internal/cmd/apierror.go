package cmd

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// APIError wraps a non-2xx Feedless API response as an error carrying
// feedctl's exit-code mapping and stderr rendering: the status code, plus —
// on a best-effort basis — its ApiError-shaped JSON body (message, code,
// field errors; see openapi.yaml's ApiError schema). Every command that
// calls the generated API client, and feedctl api's raw requests, builds
// one of these for a non-2xx response instead of inventing its own error
// text, so every command reports API errors the same way. This is C3's one
// error mapping — commands never format an API failure themselves.
type APIError struct {
	Status int
	Body   api.ApiError
	// What names the resource for the 404 message ("not found: <what>") —
	// e.g. "source <id>". Ignored for every other status.
	What string
}

// apiResponse is satisfied by every internal/api …Response type the
// generated WithResponse client returns (ListSourcesResponse,
// GetSourceResponse, …) — oapi-codegen gives each of them these two methods
// regardless of operation. NewAPIError takes this instead of a concrete
// type so any command can pass its response straight through.
type apiResponse interface {
	StatusCode() int
	GetBody() []byte
}

// NewAPIError builds an *APIError from resp when its status is not 2xx,
// parsing resp's raw body as api.ApiError on a best-effort basis — an empty
// or non-JSON body just leaves Body.Message "". It returns nil for a 2xx
// response, so a command can call it unconditionally right after checking
// the transport-level error:
//
//	resp, err := apiClient.API.GetSourceWithResponse(ctx, repoID, sourceID)
//	if err != nil {
//	    return err
//	}
//	if apiErr := cmd.NewAPIError(resp, fmt.Sprintf("source %s", sourceID)); apiErr != nil {
//	    return apiErr
//	}
func NewAPIError(resp apiResponse, what string) error {
	status := resp.StatusCode()
	if status >= http.StatusOK && status < http.StatusMultipleChoices {
		return nil
	}

	var body api.ApiError

	_ = json.Unmarshal(resp.GetBody(), &body)

	return &APIError{Status: status, Body: body, What: what}
}

// Error renders the one-line message: "not found: <what>" for a 404
// (ignoring the body — the brief's exact wording), the server's message
// otherwise, falling back to the HTTP status text when the body carried
// none.
func (e *APIError) Error() string {
	if e.Status == http.StatusNotFound {
		return "not found: " + e.what()
	}

	if e.Body.Message != "" {
		return e.Body.Message
	}

	return http.StatusText(e.Status)
}

func (e *APIError) what() string {
	if e.What != "" {
		return e.What
	}

	return "resource"
}

// ExitCode implements the exitCoder interface main.go's run() looks for: 4
// when the API rejected the request as unauthenticated (matching
// config.NotLoggedInError and every ExitError-based auth failure), 1
// otherwise — including 404, which is a normal failure, not an auth
// problem.
func (e *APIError) ExitCode() int {
	if e.Status == http.StatusUnauthorized {
		return ExitAuthRequired
	}

	return 1
}

// RenderError implements the errorRenderer interface main.go's run() looks
// for: the message line from Error(), plus one indented "field: message"
// line per field error (present on VALIDATION_ERROR responses; absent
// otherwise).
func (e *APIError) RenderError(w io.Writer) {
	_, _ = fmt.Fprintf(w, "error: %s\n", e.Error())

	if e.Body.Errors == nil {
		return
	}

	for _, fe := range *e.Body.Errors {
		_, _ = fmt.Fprintf(w, "  %s: %s\n", fe.Field, fe.Message)
	}
}
