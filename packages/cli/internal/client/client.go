// Package client builds the authenticated HTTP client every feedctl
// command uses to talk to a Feedless instance's /api/v1: it wraps the
// generated internal/api client with two things that must apply to every
// request regardless of which command makes it — the transport guard
// (never send credentials over plain http to a non-loopback host) and a
// once-per-invocation warning when the server's X-Feedless-Version
// disagrees with this binary's version (shared across every Client a
// command builds via Warner, since a command like auth status builds one
// Client per configured host).
package client

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"sync/atomic"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/config"
)

// Client bundles the generated API client with the host and base URL it
// was built for, plus what `feedctl api` needs to make raw requests through
// the same transport (guard + version warning) and auth as the generated
// client: HTTP is the guarded *http.Client, APIBaseURL is rawURL + "/api/v1"
// with no trailing slash, and Authorize sets the bearer token header a raw
// *http.Request needs.
type Client struct {
	API        api.ClientWithResponsesInterface
	Host       string
	URL        string
	HTTP       *http.Client
	APIBaseURL string
	token      string
}

// Authorize sets the Authorization header on req the same way every
// request the generated API client makes is authorized (see New's token
// parameter). It's a no-op when the Client was built without a token.
func (c *Client) Authorize(req *http.Request) {
	if c.token != "" {
		req.Header.Set("Authorization", "Bearer "+c.token)
	}
}

// Warner gates the version-mismatch warning (see New) across every Client
// built during one feedctl invocation, so it prints at most once in total
// — not once per Client. A command that builds several Clients in one run
// (auth status, looping over configured hosts) must create exactly one
// Warner with NewWarner and pass it to every New call; a command that
// builds only one Client can pass its own fresh NewWarner() (or use
// NewFromConfig, which does this for it).
type Warner struct {
	fired atomic.Bool
}

// NewWarner returns a Warner that hasn't fired yet.
func NewWarner() *Warner {
	return &Warner{}
}

// fire writes msg to stderr the first time it is called on this Warner —
// from this call or, once the Warner is shared across Clients, from any of
// them — and is a silent no-op on every call after that.
func (w *Warner) fire(stderr io.Writer, msg string) {
	if w.fired.CompareAndSwap(false, true) {
		_, _ = fmt.Fprint(stderr, msg)
	}
}

// New builds a Client for host (the hosts.yml key / --host value) at
// rawURL, authenticating requests with token (pass "" for none). Every
// request goes through the transport guard and the version-mismatch
// check; a nil warner gets a private one, equivalent to NewWarner().
func New(host, rawURL, token, cliVersion string, stderr io.Writer, warner *Warner) (*Client, error) {
	if warner == nil {
		warner = NewWarner()
	}

	base := strings.TrimRight(rawURL, "/")

	transport := &guardedTransport{
		next:       http.DefaultTransport,
		host:       host,
		baseURL:    base,
		cliVersion: cliVersion,
		stderr:     stderr,
		warner:     warner,
	}

	opts := []api.ClientOption{api.WithHTTPClient(&http.Client{Transport: transport})}
	if token != "" {
		opts = append(opts, api.WithRequestEditorFn(func(_ context.Context, req *http.Request) error {
			req.Header.Set("Authorization", "Bearer "+token)
			return nil
		}))
	}

	apiBaseURL := base + "/api/v1"

	apiClient, err := api.NewClientWithResponses(apiBaseURL, opts...)
	if err != nil {
		return nil, fmt.Errorf("building API client for %s: %w", host, err)
	}

	return &Client{
		API:        apiClient,
		Host:       host,
		URL:        rawURL,
		HTTP:       &http.Client{Transport: transport},
		APIBaseURL: apiBaseURL,
		token:      token,
	}, nil
}

// NewFromConfig resolves the host and token to use per feedctl's standard
// resolution order (config.Resolve) and builds an authenticated Client for
// it, with its own private Warner (this function only ever builds one
// Client, so there is nothing to share it with). This is the entry point
// later commands use to get a ready client; it returns
// *config.NotLoggedInError, unwrapped, when nothing is resolved.
func NewFromConfig(cfg *config.Config, flagHost, cliVersion string, stderr io.Writer) (*Client, *config.Resolved, error) {
	resolved, err := config.Resolve(cfg, flagHost)
	if err != nil {
		return nil, nil, err
	}

	c, err := New(resolved.Host, resolved.URL, resolved.Token, cliVersion, stderr, NewWarner())
	if err != nil {
		return nil, nil, err
	}

	return c, resolved, nil
}

// guardedTransport wraps an http.RoundTripper with the transport guard and
// the version-mismatch check. It only ever examines the first response it
// sees (checked) — later responses on the same Client are never
// inspected, matching "after the first response of an invocation" — and
// prints through warner, which is what actually limits printing to once
// per invocation when several Clients share it.
type guardedTransport struct {
	next       http.RoundTripper
	host       string
	baseURL    string
	cliVersion string
	stderr     io.Writer
	warner     *Warner
	checked    bool
}

func (t *guardedTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	if err := guardScheme(req.URL); err != nil {
		return nil, err
	}

	resp, err := t.next.RoundTrip(req)
	if err != nil {
		return resp, err
	}

	if !t.checked {
		t.checked = true
		t.warnOnVersionMismatch(resp)
	}

	return resp, nil
}

func (t *guardedTransport) warnOnVersionMismatch(resp *http.Response) {
	serverVersion := resp.Header.Get("X-Feedless-Version")
	if serverVersion == "" || serverVersion == t.cliVersion {
		return
	}
	if serverVersion == "dev" || t.cliVersion == "dev" {
		return
	}

	msg := fmt.Sprintf("warning: feedctl %s differs from %s version %s; download the matching build from %s/cli/\n",
		t.cliVersion, t.host, serverVersion, t.baseURL)
	t.warner.fire(t.stderr, msg)
}

// guardScheme refuses to let a request go out over plain http unless it
// targets a loopback host — never send the token over the network in the
// clear.
func guardScheme(u *url.URL) error {
	if u.Scheme != "http" {
		return nil
	}
	if isLoopback(u.Hostname()) {
		return nil
	}

	return fmt.Errorf("refusing to send request over plain http to %s: use https, or target localhost/127.0.0.1/::1", u.Host)
}

func isLoopback(hostname string) bool {
	return hostname == "localhost" || hostname == "127.0.0.1" || hostname == "::1"
}
