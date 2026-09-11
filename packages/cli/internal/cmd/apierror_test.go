package cmd

import (
	"bytes"
	"net/http"
	"strings"
	"testing"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// fakeResponse is a minimal apiResponse for tests that don't need a real
// generated …Response type.
type fakeResponse struct {
	status int
	body   []byte
}

func (r fakeResponse) StatusCode() int { return r.status }
func (r fakeResponse) GetBody() []byte { return r.body }

func TestNewAPIError_2xx_ReturnsNil(t *testing.T) {
	if err := NewAPIError(fakeResponse{status: http.StatusOK, body: nil}, "x"); err != nil {
		t.Errorf("NewAPIError() = %v, want nil for a 2xx response", err)
	}
}

func TestNewAPIError_401_ExitCode4(t *testing.T) {
	resp := fakeResponse{status: http.StatusUnauthorized, body: []byte(`{"code":"UNAUTHORIZED","message":"invalid token"}`)}

	err := NewAPIError(resp, "x")
	if err == nil {
		t.Fatal("NewAPIError() = nil, want an error for a 401 response")
	}

	apiErr, ok := err.(*APIError)
	if !ok {
		t.Fatalf("NewAPIError() type = %T, want *APIError", err)
	}

	if code := apiErr.ExitCode(); code != 4 {
		t.Errorf("ExitCode() = %d, want 4", code)
	}
	if got := apiErr.Error(); got != "invalid token" {
		t.Errorf("Error() = %q, want %q", got, "invalid token")
	}
}

func TestNewAPIError_404_MessageIsNotFoundWhat(t *testing.T) {
	resp := fakeResponse{status: http.StatusNotFound, body: []byte(`{"code":"NOT_FOUND","message":"no such thing"}`)}

	err := NewAPIError(resp, "source abc123")
	if err == nil {
		t.Fatal("NewAPIError() = nil, want an error for a 404 response")
	}

	want := "not found: source abc123"
	if got := err.Error(); got != want {
		t.Errorf("Error() = %q, want %q (ignoring the server's own message)", got, want)
	}

	apiErr := err.(*APIError)
	if code := apiErr.ExitCode(); code != 1 {
		t.Errorf("ExitCode() = %d, want 1 for 404", code)
	}
}

func TestNewAPIError_ValidationError_ExitCode1(t *testing.T) {
	resp := fakeResponse{
		status: http.StatusBadRequest,
		body:   []byte(`{"code":"VALIDATION_ERROR","message":"invalid request","errors":[{"field":"url","message":"must not be blank"}]}`),
	}

	err := NewAPIError(resp, "")
	apiErr := err.(*APIError)

	if code := apiErr.ExitCode(); code != 1 {
		t.Errorf("ExitCode() = %d, want 1", code)
	}
}

func TestAPIError_RenderError_PrintsMessageAndFieldErrors(t *testing.T) {
	apiErr := &APIError{
		Status: http.StatusBadRequest,
		Body: api.ApiError{
			Message: "invalid request",
			Errors: &[]api.FieldError{
				{Field: "url", Message: "must not be blank"},
				{Field: "title", Message: "too long"},
			},
		},
	}

	buf := &bytes.Buffer{}
	apiErr.RenderError(buf)

	got := buf.String()
	want := "error: invalid request\n  url: must not be blank\n  title: too long\n"
	if got != want {
		t.Errorf("RenderError() =\n%q\nwant\n%q", got, want)
	}
}

func TestAPIError_RenderError_NoFieldErrors_OnlyPrintsMessage(t *testing.T) {
	apiErr := &APIError{Status: http.StatusInternalServerError, Body: api.ApiError{Message: "boom"}}

	buf := &bytes.Buffer{}
	apiErr.RenderError(buf)

	want := "error: boom\n"
	if got := buf.String(); got != want {
		t.Errorf("RenderError() = %q, want %q", got, want)
	}
}

// --- C9 fix round 1: server error text must not reach stderr unsanitized ---

func TestAPIError_RenderError_SanitizesMessageAndFieldErrors(t *testing.T) {
	esc, bel := string(rune(0x1b)), string(rune(0x07))

	apiErr := &APIError{
		Status: http.StatusBadRequest,
		Body: api.ApiError{
			Message: "invalid request " + esc + "]0;pwned" + bel + "here",
			Errors: &[]api.FieldError{
				{Field: "url", Message: `must not contain "` + esc + "[31mred" + esc + "[0m\""},
			},
		},
	}

	buf := &bytes.Buffer{}
	apiErr.RenderError(buf)

	got := buf.String()
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("RenderError() = %q, want no raw ESC (0x1b) reaching stderr", got)
	}
	if strings.ContainsRune(got, 0x07) {
		t.Errorf("RenderError() = %q, want no raw BEL (0x07) reaching stderr", got)
	}
	if !strings.Contains(got, "invalid request") || !strings.Contains(got, "here") {
		t.Errorf("RenderError() = %q, want the text around the stripped OSC sequence preserved", got)
	}
	if !strings.Contains(got, "url:") || !strings.Contains(got, "red") {
		t.Errorf("RenderError() = %q, want the field error's text preserved (with the CSI colour stripped)", got)
	}
}

func TestAPIError_Error_SanitizesMessage(t *testing.T) {
	esc := string(rune(0x1b))

	apiErr := &APIError{
		Status: http.StatusBadRequest,
		Body:   api.ApiError{Message: esc + "[2Jwiped " + esc + "[31mtext"},
	}

	got := apiErr.Error()
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("Error() = %q, want no raw ESC (0x1b)", got)
	}
	if !strings.Contains(got, "wiped") || !strings.Contains(got, "text") {
		t.Errorf("Error() = %q, want the surrounding text preserved", got)
	}
}

func TestNewAPIError_EmptyBody_FallsBackToStatusText(t *testing.T) {
	resp := fakeResponse{status: http.StatusInternalServerError, body: nil}

	err := NewAPIError(resp, "")
	if err == nil {
		t.Fatal("NewAPIError() = nil, want an error")
	}

	if got := err.Error(); !strings.Contains(got, "Internal Server Error") {
		t.Errorf("Error() = %q, want it to fall back to the HTTP status text", got)
	}
}
