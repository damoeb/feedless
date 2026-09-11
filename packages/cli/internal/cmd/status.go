package cmd

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/config"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// ExitUsage shares its value with ExitCancelled.
const ExitUsage = 2

// statusBuildTimeLayout renders the server's build time, always in UTC.
const statusBuildTimeLayout = "2006-01-02 15:04 UTC"

// newStatusCmd needs no login; it exits 0 only if an agent is connected, so scripts can gate on it.
func newStatusCmd(version string) *cobra.Command {
	var asJSON bool

	var jq string

	statusCmd := &cobra.Command{
		Use:   "status",
		Short: "Show a Feedless host's version and how many prerender agents are connected",
		Long: `Show a Feedless host's version, build and number of connected prerender agents.

No login is needed. The host is --host, else FEEDCTL_HOST, else the default
(or only) configured host. --host takes a configured host name, any host name
(reached over https) or a URL such as http://localhost:8080.

Exits 1 when the host cannot be reached, answers an error, or has no
prerender agent connected.`,
		Args: cobra.NoArgs,
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runStatus(cmd, version, asJSON, jq)
		},
	}

	statusCmd.Flags().BoolVar(&asJSON, "json", false, "Print the server's JSON response")
	statusCmd.Flags().StringVar(&jq, "jq", "", "Filter the --json output with a jq expression (requires --json)")

	return statusCmd
}

func runStatus(cmd *cobra.Command, version string, asJSON bool, jq string) error {
	if jq != "" && !asJSON {
		return errors.New("--jq requires --json")
	}

	host, baseURL, err := resolveStatusTarget(cmd)
	if err != nil {
		return err
	}

	// No token, even when one is configured: the endpoint is public, and
	// reading one could mean an OS keyring prompt for nothing.
	apiClient, err := client.New(host, baseURL, "", version, cmd.ErrOrStderr(), client.NewWarner())
	if err != nil {
		return err
	}

	resp, err := apiClient.API.GetStatusWithResponse(cmd.Context())
	if err != nil {
		// Only a request that never got an answer is a *url.Error; a JSON parse failure isn't.
		var urlErr *url.Error
		if errors.As(err, &urlErr) {
			return fmt.Errorf("contacting %s: %w", host, err)
		}

		return NewExitError(1, "status of %s: invalid server response: %s", host, output.SafeText(err.Error()))
	}

	if apiErr := NewAPIError(resp, "GET /api/v1/status"); apiErr != nil {
		msg := apiErr.Error()

		// Older instances answer 404 or 401 for GET /status; exit 1, since logging in wouldn't help.
		if code := resp.StatusCode(); code == http.StatusUnauthorized || code == http.StatusNotFound {
			msg += " (the instance may predate GET /api/v1/status)"
		}

		return NewExitError(1, "status of %s: %s", host, msg)
	}

	if resp.JSON200 == nil {
		return NewExitError(1, "status of %s: invalid server response (%s, %s)", host, resp.Status(),
			output.SafeText(resp.HTTPResponse.Header.Get("Content-Type")))
	}

	if asJSON {
		err = printStatusJSON(cmd.OutOrStdout(), resp.Body, jq)
	} else {
		err = renderStatus(cmd.OutOrStdout(), baseURL, version, *resp.JSON200)
	}

	if err != nil {
		return err
	}

	if resp.JSON200.Agents.Connected < 1 {
		return NewExitError(1, "no prerender agent connected")
	}

	return nil
}

// Configured hosts use their stored URL, absolute URLs are used as given, other names get https.
func resolveStatusTarget(cmd *cobra.Command) (host, baseURL string, err error) {
	flagHost, _ := cmd.Flags().GetString("host")

	cfg, err := config.Load()
	if err != nil {
		return "", "", err
	}

	host = config.ResolveHost(cfg, flagHost)
	if host == "" && len(cfg.Hosts) == 1 {
		for h := range cfg.Hosts {
			host = h
		}
	}

	if host == "" {
		return "", "", NewExitError(ExitUsage,
			"no host to ask: pass --host <host or URL>, set %s, or log in with: feedctl auth login --url <url>", config.EnvHost)
	}

	if entry, ok := cfg.Hosts[host]; ok && entry.URL != "" {
		return host, strings.TrimRight(entry.URL, "/"), nil
	}

	if !strings.Contains(host, "://") {
		return host, "https://" + host, nil
	}

	parsed, err := url.Parse(host)
	if err != nil || parsed.Host == "" || (parsed.Scheme != "http" && parsed.Scheme != "https") {
		return "", "", NewExitError(ExitUsage, "invalid host %q: pass a host name or an absolute http(s) URL", host)
	}

	return parsed.Host, strings.TrimRight(host, "/"), nil
}

// Version and commit are server-provided, so they're sanitized.
func renderStatus(w io.Writer, baseURL, cliVersion string, s api.ServerStatus) error {
	built := time.UnixMilli(s.Build.Date).UTC().Format(statusBuildTimeLayout)

	lines := []struct{ label, value string }{
		{"Server:", baseURL},
		{"Version:", fmt.Sprintf("%s (commit %s, built %s)", output.SafeText(s.Version), output.SafeText(s.Build.Commit), built)},
		{"CLI:", cliVersion},
		{"Agents:", fmt.Sprintf("%d connected", s.Agents.Connected)},
	}

	for _, l := range lines {
		if _, err := fmt.Fprintf(w, "%-10s%s\n", l.label, l.value); err != nil {
			return fmt.Errorf("writing status: %w", err)
		}
	}

	return nil
}

func printStatusJSON(w io.Writer, body []byte, jq string) error {
	if jq != "" {
		var generic any
		if err := json.Unmarshal(body, &generic); err != nil {
			return fmt.Errorf("decoding the status response for --jq: %w", err)
		}

		return output.RunJQ(w, jq, generic)
	}

	var buf bytes.Buffer
	if err := json.Indent(&buf, body, "", "  "); err != nil {
		return fmt.Errorf("formatting the status response: %w", err)
	}

	buf.WriteByte('\n')

	if _, err := w.Write(buf.Bytes()); err != nil {
		return fmt.Errorf("writing status: %w", err)
	}

	return nil
}
