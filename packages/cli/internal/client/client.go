// Package client builds the authenticated HTTP client every feedctl
// command uses to talk to a Feedless instance's /api/v1: it wraps the
// generated internal/api client with two things that must apply to every
// request regardless of which command makes it — the transport guard
// (never send credentials over plain http to a non-loopback host) and a
// one-time warning when the server's X-Feedless-Version disagrees with
// this binary's version.
package client

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/config"
)

// Client bundles the generated API client with the host and base URL it
// was built for.
type Client struct {
	API  api.ClientWithResponsesInterface
	Host string
	URL  string
}

// New builds a Client for host (the hosts.yml key / --host value) at
// rawURL, authenticating requests with token (pass "" for none). Every
// request goes through the transport guard and the version-mismatch
// check; version-mismatch warnings are written to stderr.
func New(host, rawURL, token, cliVersion string, stderr io.Writer) (*Client, error) {
	base := strings.TrimRight(rawURL, "/")

	transport := &guardedTransport{
		next:       http.DefaultTransport,
		host:       host,
		baseURL:    base,
		cliVersion: cliVersion,
		stderr:     stderr,
	}

	opts := []api.ClientOption{api.WithHTTPClient(&http.Client{Transport: transport})}
	if token != "" {
		opts = append(opts, api.WithRequestEditorFn(func(_ context.Context, req *http.Request) error {
			req.Header.Set("Authorization", "Bearer "+token)
			return nil
		}))
	}

	apiClient, err := api.NewClientWithResponses(base+"/api/v1", opts...)
	if err != nil {
		return nil, fmt.Errorf("building API client for %s: %w", host, err)
	}

	return &Client{API: apiClient, Host: host, URL: rawURL}, nil
}

// NewFromConfig resolves the host and token to use per feedctl's standard
// resolution order (config.Resolve) and builds an authenticated Client for
// it. This is the entry point later commands use to get a ready client;
// it returns *config.NotLoggedInError, unwrapped, when nothing is
// resolved.
func NewFromConfig(cfg *config.Config, flagHost, cliVersion string, stderr io.Writer) (*Client, *config.Resolved, error) {
	resolved, err := config.Resolve(cfg, flagHost)
	if err != nil {
		return nil, nil, err
	}

	c, err := New(resolved.Host, resolved.URL, resolved.Token, cliVersion, stderr)
	if err != nil {
		return nil, nil, err
	}

	return c, resolved, nil
}

// guardedTransport wraps an http.RoundTripper with the transport guard and
// the once-per-Client version warning.
type guardedTransport struct {
	next       http.RoundTripper
	host       string
	baseURL    string
	cliVersion string
	stderr     io.Writer
	warned     bool
}

func (t *guardedTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	if err := guardScheme(req.URL); err != nil {
		return nil, err
	}

	resp, err := t.next.RoundTrip(req)
	if err != nil {
		return resp, err
	}

	if !t.warned {
		t.warned = true
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

	_, _ = fmt.Fprintf(t.stderr, "warning: feedctl %s differs from %s version %s; download the matching build from %s/cli/\n",
		t.cliVersion, t.host, serverVersion, t.baseURL)
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
