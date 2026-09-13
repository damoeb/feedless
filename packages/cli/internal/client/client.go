// Package client guards every request (no credentials over plain http to a remote host) and warns once on a server version mismatch.
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

// Client exposes HTTP and Authorize so `feedctl api` sends raw requests through the same transport and auth.
type Client struct {
	API        api.ClientWithResponsesInterface
	Host       string
	URL        string
	HTTP       *http.Client
	APIBaseURL string
	token      string
}

func (c *Client) Authorize(req *http.Request) {
	if c.token != "" {
		req.Header.Set("Authorization", "Bearer "+c.token)
	}
}

// Warner prints the version warning once per invocation; share one across all Clients a command builds.
type Warner struct {
	fired atomic.Bool
}

func NewWarner() *Warner {
	return &Warner{}
}

func (w *Warner) fire(stderr io.Writer, msg string) {
	if w.fired.CompareAndSwap(false, true) {
		_, _ = fmt.Fprint(stderr, msg)
	}
}

// A nil warner gets a private one.
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

// NewFromConfig returns *config.NotLoggedInError, unwrapped, when nothing resolves.
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

// guardedTransport inspects only the first response for the version header.
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

// Never send the token in the clear: plain http only to loopback.
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
