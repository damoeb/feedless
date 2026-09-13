package client

import (
	"bytes"
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/damoeb/feedless/packages/cli/internal/config"
)

func newUserServer(t *testing.T, serverVersion string, handler func(w http.ResponseWriter, r *http.Request)) *httptest.Server {
	t.Helper()

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if serverVersion != "" {
			w.Header().Set("X-Feedless-Version", serverVersion)
		}
		if handler != nil {
			handler(w, r)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"id":"11111111-1111-1111-1111-111111111111","email":"someone@example.org","groups":[]}`))
	}))
	t.Cleanup(srv.Close)

	return srv
}

func TestNew_SendsBearerToken(t *testing.T) {
	var gotAuth string

	srv := newUserServer(t, "", func(w http.ResponseWriter, r *http.Request) {
		gotAuth = r.Header.Get("Authorization")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"id":"11111111-1111-1111-1111-111111111111","email":"someone@example.org","groups":[]}`))
	})

	c, err := New("h.example.org", srv.URL, "the-token", "1.0.0", &bytes.Buffer{}, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	resp, err := c.API.GetAuthenticatedUserWithResponse(context.Background())
	if err != nil {
		t.Fatalf("GetAuthenticatedUserWithResponse() error = %v", err)
	}
	if resp.JSON200 == nil {
		t.Fatalf("JSON200 = nil, body = %s", resp.Body)
	}

	if gotAuth != "Bearer the-token" {
		t.Errorf("Authorization header = %q, want %q", gotAuth, "Bearer the-token")
	}
}

func TestNew_NoToken_OmitsAuthorizationHeader(t *testing.T) {
	var gotAuth string
	var sawAuthHeader bool

	srv := newUserServer(t, "", func(w http.ResponseWriter, r *http.Request) {
		gotAuth, sawAuthHeader = r.Header.Get("Authorization"), r.Header.Get("Authorization") != ""
		w.WriteHeader(http.StatusUnauthorized)
	})

	c, err := New("h.example.org", srv.URL, "", "1.0.0", &bytes.Buffer{}, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	_, _ = c.API.GetAuthenticatedUserWithResponse(context.Background())

	if sawAuthHeader {
		t.Errorf("Authorization header = %q, want none", gotAuth)
	}
}

func TestGuardScheme_RefusesNonLoopbackHTTP(t *testing.T) {
	c, err := New("evil.example.org", "http://evil.example.org", "the-token", "1.0.0", &bytes.Buffer{}, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	_, err = c.API.GetAuthenticatedUserWithResponse(context.Background())
	if err == nil {
		t.Fatal("GetAuthenticatedUserWithResponse() error = nil, want a transport-guard error")
	}
	if !strings.Contains(err.Error(), "plain http") {
		t.Errorf("error = %v, want it to mention plain http", err)
	}
}

func TestGuardScheme_AllowsLoopbackHTTP(t *testing.T) {
	// httptest.NewServer listens on 127.0.0.1, i.e. loopback http.
	srv := newUserServer(t, "", nil)

	c, err := New("localhost", srv.URL, "the-token", "1.0.0", &bytes.Buffer{}, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	resp, err := c.API.GetAuthenticatedUserWithResponse(context.Background())
	if err != nil {
		t.Fatalf("GetAuthenticatedUserWithResponse() error = %v", err)
	}
	if resp.JSON200 == nil {
		t.Fatalf("JSON200 = nil, body = %s", resp.Body)
	}
}

func TestGuardScheme_AllowsHTTPS(t *testing.T) {
	// https is never guarded; an undialable URL shows the guard didn't fire.
	c, err := New("unreachable.invalid", "https://127.0.0.1:1", "the-token", "1.0.0", &bytes.Buffer{}, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	_, err = c.API.GetAuthenticatedUserWithResponse(context.Background())
	if err == nil {
		t.Fatal("GetAuthenticatedUserWithResponse() error = nil, want a dial error")
	}
	if strings.Contains(err.Error(), "plain http") {
		t.Errorf("error = %v, guard must not fire for https", err)
	}
}

func TestVersionWarning_PrintedOnceWhenVersionsDiffer(t *testing.T) {
	srv := newUserServer(t, "9.9.9", nil)
	stderr := &bytes.Buffer{}

	c, err := New("h.example.org", srv.URL, "the-token", "1.0.0", stderr, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("request 1 error = %v", err)
	}
	if _, err := c.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("request 2 error = %v", err)
	}

	out := stderr.String()
	if got := strings.Count(out, "warning:"); got != 1 {
		t.Errorf("warning count = %d, want 1; stderr = %q", got, out)
	}
	if !strings.Contains(out, "feedctl 1.0.0 differs from h.example.org version 9.9.9") {
		t.Errorf("stderr = %q, want it to name both versions and the host", out)
	}
	if !strings.Contains(out, srv.URL+"/cli/") {
		t.Errorf("stderr = %q, want it to point at %s/cli/", out, srv.URL)
	}
}

func TestVersionWarning_NotPrintedWhenVersionsMatch(t *testing.T) {
	srv := newUserServer(t, "1.0.0", nil)
	stderr := &bytes.Buffer{}

	c, err := New("h.example.org", srv.URL, "the-token", "1.0.0", stderr, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("request error = %v", err)
	}

	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want empty", stderr.String())
	}
}

func TestVersionWarning_NotPrintedForDevBuilds(t *testing.T) {
	srv := newUserServer(t, "9.9.9", nil)
	stderr := &bytes.Buffer{}

	c, err := New("h.example.org", srv.URL, "the-token", "dev", stderr, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("request error = %v", err)
	}

	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want empty for a dev build", stderr.String())
	}
}

func TestVersionWarning_NotPrintedForDevServer(t *testing.T) {
	srv := newUserServer(t, "dev", nil)
	stderr := &bytes.Buffer{}

	c, err := New("h.example.org", srv.URL, "the-token", "1.0.0", stderr, nil)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("request error = %v", err)
	}

	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want empty when the server reports dev", stderr.String())
	}
}

func TestVersionWarning_SharedWarner_PrintsOnceAcrossMultipleClients(t *testing.T) {
	srv1 := newUserServer(t, "9.9.1", nil)
	srv2 := newUserServer(t, "9.9.2", nil)
	stderr := &bytes.Buffer{}
	warner := NewWarner()

	c1, err := New("h1.example.org", srv1.URL, "the-token", "1.0.0", stderr, warner)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}
	c2, err := New("h2.example.org", srv2.URL, "the-token", "1.0.0", stderr, warner)
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c1.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("host 1 request error = %v", err)
	}
	if _, err := c2.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("host 2 request error = %v", err)
	}

	out := stderr.String()
	if got := strings.Count(out, "warning:"); got != 1 {
		t.Errorf("warning count = %d, want exactly 1 across both hosts; stderr = %q", got, out)
	}
}

// Control for the test above.
func TestVersionWarning_SeparateWarners_PrintsOncePerClient(t *testing.T) {
	srv1 := newUserServer(t, "9.9.1", nil)
	srv2 := newUserServer(t, "9.9.2", nil)
	stderr := &bytes.Buffer{}

	c1, err := New("h1.example.org", srv1.URL, "the-token", "1.0.0", stderr, NewWarner())
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}
	c2, err := New("h2.example.org", srv2.URL, "the-token", "1.0.0", stderr, NewWarner())
	if err != nil {
		t.Fatalf("New() error = %v", err)
	}

	if _, err := c1.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("host 1 request error = %v", err)
	}
	if _, err := c2.API.GetAuthenticatedUserWithResponse(context.Background()); err != nil {
		t.Fatalf("host 2 request error = %v", err)
	}

	out := stderr.String()
	if got := strings.Count(out, "warning:"); got != 2 {
		t.Errorf("warning count = %d, want 2 (one per independent Warner); stderr = %q", got, out)
	}
}

func TestNewFromConfig_NotLoggedIn_ReturnsNotLoggedInError(t *testing.T) {
	cfg := &config.Config{}
	t.Setenv(config.EnvHost, "")
	t.Setenv(config.EnvToken, "")

	_, _, err := NewFromConfig(cfg, "", "1.0.0", &bytes.Buffer{})

	var nle *config.NotLoggedInError
	if !errors.As(err, &nle) {
		t.Fatalf("NewFromConfig() error = %v, want *config.NotLoggedInError", err)
	}
}

func TestNewFromConfig_Success(t *testing.T) {
	srv := newUserServer(t, "", nil)

	cfg := &config.Config{
		DefaultHost: "h.example.org",
		Hosts:       map[string]config.HostEntry{"h.example.org": {URL: srv.URL, Token: "the-token"}},
	}
	t.Setenv(config.EnvHost, "")
	t.Setenv(config.EnvToken, "")

	c, resolved, err := NewFromConfig(cfg, "", "1.0.0", &bytes.Buffer{})
	if err != nil {
		t.Fatalf("NewFromConfig() error = %v", err)
	}
	if resolved.Host != "h.example.org" || resolved.Token != "the-token" {
		t.Errorf("resolved = %+v, unexpected", resolved)
	}

	resp, err := c.API.GetAuthenticatedUserWithResponse(context.Background())
	if err != nil {
		t.Fatalf("GetAuthenticatedUserWithResponse() error = %v", err)
	}
	if resp.JSON200 == nil {
		t.Fatalf("JSON200 = nil, body = %s", resp.Body)
	}
}
