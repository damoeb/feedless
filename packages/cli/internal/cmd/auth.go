package cmd

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/url"
	"os"
	"sort"
	"strings"

	"github.com/spf13/cobra"
	"golang.org/x/term"

	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/config"
)

func newAuthCmd(version string) *cobra.Command {
	auth := &cobra.Command{
		Use:   "auth",
		Short: "Log in to, check, or log out of a Feedless host",
	}

	auth.AddCommand(newAuthLoginCmd(version))
	auth.AddCommand(newAuthStatusCmd(version))
	auth.AddCommand(newAuthLogoutCmd())

	return auth
}

func newAuthLoginCmd(version string) *cobra.Command {
	var rawURL string
	var withToken bool

	loginCmd := &cobra.Command{
		Use:   "login",
		Short: "Log in to a Feedless instance",
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runAuthLogin(cmd, version, rawURL, withToken)
		},
	}

	loginCmd.Flags().StringVar(&rawURL, "url", "", "URL of the Feedless instance to log in to (required)")
	loginCmd.Flags().BoolVar(&withToken, "with-token", false, "read the token from stdin instead of prompting")
	_ = loginCmd.MarkFlagRequired("url")

	return loginCmd
}

func runAuthLogin(cmd *cobra.Command, version, rawURL string, withToken bool) error {
	host, err := hostFromURL(rawURL)
	if err != nil {
		return err
	}

	token, err := readToken(cmd, withToken)
	if err != nil {
		return err
	}

	apiClient, err := client.New(host, rawURL, token, version, cmd.ErrOrStderr(), client.NewWarner())
	if err != nil {
		return err
	}

	email, err := verifyToken(cmd.Context(), apiClient, host)
	if err != nil {
		return err
	}

	cfg, err := config.Load()
	if err != nil {
		return err
	}

	isFirstHost := len(cfg.Hosts) == 0

	_, fileToken, err := config.StoreToken(host, token, cmd.ErrOrStderr())
	if err != nil {
		return err
	}

	cfg.Hosts[host] = config.HostEntry{URL: rawURL, User: email, Token: fileToken}
	if isFirstHost {
		cfg.DefaultHost = host
	}

	if err := cfg.Save(); err != nil {
		return err
	}

	_, _ = fmt.Fprintf(cmd.OutOrStdout(), "Logged in to %s as %s\n", host, email)

	return nil
}

func hostFromURL(rawURL string) (string, error) {
	parsed, err := url.Parse(rawURL)
	if err != nil || parsed.Host == "" || (parsed.Scheme != "http" && parsed.Scheme != "https") {
		return "", fmt.Errorf("invalid --url %q: must be an absolute http(s) URL", rawURL)
	}

	return parsed.Host, nil
}

// Without a terminal, reading interactively is refused rather than silently blocking.
func readToken(cmd *cobra.Command, withToken bool) (string, error) {
	if withToken {
		data, err := io.ReadAll(cmd.InOrStdin())
		if err != nil {
			return "", fmt.Errorf("reading token from stdin: %w", err)
		}

		token := strings.TrimSpace(string(data))
		if token == "" {
			return "", errors.New("no token read from stdin")
		}

		return token, nil
	}

	stdin, ok := cmd.InOrStdin().(*os.File)
	if !ok || !term.IsTerminal(int(stdin.Fd())) {
		return "", errors.New("stdin is not a terminal; pass --with-token to read the token from stdin")
	}

	_, _ = fmt.Fprint(cmd.ErrOrStderr(), "Token: ")
	data, err := term.ReadPassword(int(stdin.Fd()))
	_, _ = fmt.Fprintln(cmd.ErrOrStderr())

	if err != nil {
		return "", fmt.Errorf("reading token: %w", err)
	}

	token := strings.TrimSpace(string(data))
	if token == "" {
		return "", errors.New("no token entered")
	}

	return token, nil
}

// verifyToken exits 4 on a non-200; the caller must not store anything.
func verifyToken(ctx context.Context, apiClient *client.Client, host string) (string, error) {
	resp, err := apiClient.API.GetAuthenticatedUserWithResponse(ctx)
	if err != nil {
		return "", fmt.Errorf("contacting %s: %w", host, err)
	}

	if resp.JSON200 == nil {
		return "", NewExitError(4, "authentication failed for %s (%s)", host, resp.Status())
	}

	return string(resp.JSON200.Email), nil
}

func newAuthStatusCmd(version string) *cobra.Command {
	return &cobra.Command{
		Use:   "status",
		Short: "Show authentication status for each configured host",
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runAuthStatus(cmd, version)
		},
	}
}

func runAuthStatus(cmd *cobra.Command, version string) error {
	cfg, err := config.Load()
	if err != nil {
		return err
	}

	if len(cfg.Hosts) == 0 {
		_, _ = fmt.Fprintln(cmd.OutOrStdout(), "not logged in to any host")
		return nil
	}

	flagHost, _ := cmd.Flags().GetString("host")
	resolvedHost := config.ResolveHost(cfg, flagHost)

	hosts := make([]string, 0, len(cfg.Hosts))
	for h := range cfg.Hosts {
		hosts = append(hosts, h)
	}
	sort.Strings(hosts)

	// One Warner for all hosts, so the version warning prints once.
	warner := client.NewWarner()

	anyFailed := false
	for _, host := range hosts {
		if !reportHostStatus(cmd, version, cfg, host, host == resolvedHost, warner) {
			anyFailed = true
		}
	}

	if anyFailed {
		return NewExitError(1, "one or more hosts failed authentication")
	}

	return nil
}

func reportHostStatus(cmd *cobra.Command, version string, cfg *config.Config, host string, isResolved bool, warner *client.Warner) bool {
	entry := cfg.Hosts[host]
	token, source := config.TokenForHost(cfg, host, isResolved)

	status := "no token"
	authenticated := false

	if token != "" {
		apiClient, err := client.New(host, entry.URL, token, version, cmd.ErrOrStderr(), warner)
		switch {
		case err != nil:
			status = fmt.Sprintf("error: %s", err)
		default:
			resp, respErr := apiClient.API.GetAuthenticatedUserWithResponse(cmd.Context())
			switch {
			case respErr != nil:
				status = fmt.Sprintf("error: %s", respErr)
			case resp.JSON200 != nil:
				status = "ok"
				authenticated = true
			default:
				status = fmt.Sprintf("failed (%s)", resp.Status())
			}
		}
	}

	marker := ""
	if host == cfg.DefaultHost {
		marker = " (default)"
	}

	sourceLabel := source
	if sourceLabel == "" {
		sourceLabel = "-"
	}

	_, _ = fmt.Fprintf(cmd.OutOrStdout(), "%s%s\tuser=%s\ttoken=%s\tstatus=%s\n", host, marker, entry.User, sourceLabel, status)

	return authenticated
}

func newAuthLogoutCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "logout",
		Short: "Log out of a Feedless host",
		RunE: func(cmd *cobra.Command, _ []string) error {
			return runAuthLogout(cmd)
		},
	}
}

func runAuthLogout(cmd *cobra.Command) error {
	cfg, err := config.Load()
	if err != nil {
		return err
	}

	flagHost, _ := cmd.Flags().GetString("host")
	host := config.ResolveHost(cfg, flagHost)

	if host == "" {
		return &config.NotLoggedInError{}
	}
	if _, ok := cfg.Hosts[host]; !ok {
		return &config.NotLoggedInError{Host: host}
	}

	config.RemoveToken(host)
	delete(cfg.Hosts, host)

	if cfg.DefaultHost == host {
		cfg.DefaultHost = nextDefaultHost(cfg.Hosts)
	}

	if err := cfg.Save(); err != nil {
		return err
	}

	_, _ = fmt.Fprintf(cmd.OutOrStdout(), "Logged out of %s\n", host)

	return nil
}

func nextDefaultHost(hosts map[string]config.HostEntry) string {
	if len(hosts) == 0 {
		return ""
	}

	remaining := make([]string, 0, len(hosts))
	for h := range hosts {
		remaining = append(remaining, h)
	}
	sort.Strings(remaining)

	return remaining[0]
}
