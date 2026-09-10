package cmd

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"sort"
	"strings"

	"github.com/spf13/cobra"

	"github.com/damoeb/feedless/packages/cli/internal/client"
	"github.com/damoeb/feedless/packages/cli/internal/config"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// newAPICmd builds `feedctl api`, the raw escape hatch to /api/v1: given a
// path relative to it, it makes one authenticated HTTP request through the
// same client every other command uses (client.NewFromConfig — transport
// guard, bearer token, version warning; never a separate http.Client) and
// prints the response.
func newAPICmd(version string) *cobra.Command {
	var method string
	var fields []string
	var headers []string
	var inputPath string
	var includeHeaders bool

	apiCmd := &cobra.Command{
		Use:   "api <path>",
		Short: "Make an authenticated request to the Feedless API",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runAPI(cmd, version, apiRequest{
				path:           args[0],
				method:         method,
				methodSet:      cmd.Flags().Changed("method"),
				fields:         fields,
				headers:        headers,
				inputPath:      inputPath,
				includeHeaders: includeHeaders,
			})
		},
	}

	flags := apiCmd.Flags()
	flags.StringVarP(&method, "method", "X", "",
		"HTTP method (default GET, or POST when a body is given)")
	flags.StringArrayVarP(&fields, "field", "f", nil,
		"Add a request field (key=value, or key=@file to read a file) — a query parameter on GET, a JSON body field otherwise")
	flags.StringArrayVarP(&headers, "header", "H", nil,
		"Add a request header ('Name: value')")
	flags.StringVar(&inputPath, "input", "",
		"Read the request body verbatim from a file, or - for stdin (mutually exclusive with -f)")
	flags.BoolVarP(&includeHeaders, "include", "i", false,
		"Include the response status line and headers before the body")

	return apiCmd
}

// apiRequest is newAPICmd's flags, gathered into one value so runAPI's
// signature stays independent of cobra's flag wiring and easy to call from
// tests.
type apiRequest struct {
	path           string
	method         string
	methodSet      bool
	fields         []string
	headers        []string
	inputPath      string
	includeHeaders bool
}

func runAPI(cmd *cobra.Command, version string, req apiRequest) error {
	if len(req.fields) > 0 && req.inputPath != "" {
		return errors.New("-f and --input are mutually exclusive")
	}

	target, err := url.Parse(req.path)
	if err != nil {
		return fmt.Errorf("invalid path %q: %w", req.path, err)
	}
	if target.IsAbs() || target.Host != "" {
		return fmt.Errorf("refusing absolute URL %q: pass a path relative to /api/v1", req.path)
	}

	method := requestMethod(req)

	body, contentType, query, err := buildRequestPayload(cmd, method, req)
	if err != nil {
		return err
	}

	if len(query) > 0 {
		q := target.Query()
		for k, v := range query {
			q[k] = v
		}

		target.RawQuery = q.Encode()
	}

	flagHost, _ := cmd.Flags().GetString("host")

	cfg, err := config.Load()
	if err != nil {
		return err
	}

	apiClient, _, err := client.NewFromConfig(cfg, flagHost, version, cmd.ErrOrStderr())
	if err != nil {
		return err
	}

	reqURL := apiClient.APIBaseURL + "/" + strings.TrimPrefix(target.Path, "/")
	if target.RawQuery != "" {
		reqURL += "?" + target.RawQuery
	}

	httpReq, err := http.NewRequestWithContext(cmd.Context(), method, reqURL, body)
	if err != nil {
		return fmt.Errorf("building request: %w", err)
	}

	if contentType != "" {
		httpReq.Header.Set("Content-Type", contentType)
	}

	if err := applyHeaders(httpReq, req.headers); err != nil {
		return err
	}

	apiClient.Authorize(httpReq)

	resp, err := apiClient.HTTP.Do(httpReq)
	if err != nil {
		return fmt.Errorf("requesting %s: %w", reqURL, err)
	}
	defer func() { _ = resp.Body.Close() }()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("reading response: %w", err)
	}

	writeAPIResponse(cmd.OutOrStdout(), resp, respBody, req.includeHeaders, isTerminalWriter(cmd.OutOrStdout()))

	if apiErr := NewAPIError(rawResponse{status: resp.StatusCode, body: respBody}, req.path); apiErr != nil {
		return apiErr
	}

	return nil
}

// requestMethod picks the HTTP method: the -X flag's value when given,
// otherwise GET, or POST when a body was given (-f or --input) — matching
// "Default method: GET, or POST when a body is given."
func requestMethod(req apiRequest) string {
	if req.methodSet {
		return strings.ToUpper(req.method)
	}

	if len(req.fields) > 0 || req.inputPath != "" {
		return http.MethodPost
	}

	return http.MethodGet
}

// buildRequestPayload resolves req's body (or query parameters, on GET)
// from -f and/or --input: a body reader and its Content-Type for --input
// (sent verbatim) or -f on a non-GET method (a JSON object of the
// resolved key/value pairs); query parameters for -f on a GET method. Only
// one of the two return values is ever non-empty.
func buildRequestPayload(cmd *cobra.Command, method string, req apiRequest) (io.Reader, string, url.Values, error) {
	switch {
	case req.inputPath != "":
		data, err := readInput(cmd, req.inputPath)
		if err != nil {
			return nil, "", nil, err
		}

		return bytes.NewReader(data), "", nil, nil

	case len(req.fields) > 0:
		resolved, err := resolveFields(req.fields)
		if err != nil {
			return nil, "", nil, err
		}

		if method == http.MethodGet {
			query := url.Values{}
			for k, v := range resolved {
				query.Set(k, v)
			}

			return nil, "", query, nil
		}

		data, err := json.Marshal(resolved)
		if err != nil {
			return nil, "", nil, fmt.Errorf("encoding -f fields as JSON: %w", err)
		}

		return bytes.NewReader(data), "application/json", nil, nil

	default:
		return nil, "", nil, nil
	}
}

// readInput reads --input's value: path's file content, or stdin when path
// is "-".
func readInput(cmd *cobra.Command, path string) ([]byte, error) {
	if path == "-" {
		data, err := io.ReadAll(cmd.InOrStdin())
		if err != nil {
			return nil, fmt.Errorf("reading --input from stdin: %w", err)
		}

		return data, nil
	}

	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("reading --input %s: %w", path, err)
	}

	return data, nil
}

// resolveFields parses each -f value as key=value, resolving key=@file to
// the trimmed content of file.
func resolveFields(fields []string) (map[string]string, error) {
	out := make(map[string]string, len(fields))

	for _, f := range fields {
		key, value, ok := strings.Cut(f, "=")
		if !ok {
			return nil, fmt.Errorf("invalid -f %q: want key=value", f)
		}

		if after, isFile := strings.CutPrefix(value, "@"); isFile {
			data, err := os.ReadFile(after)
			if err != nil {
				return nil, fmt.Errorf("reading -f %s value from %s: %w", key, after, err)
			}

			value = strings.TrimRight(string(data), "\n")
		}

		out[key] = value
	}

	return out, nil
}

// applyHeaders sets each -H 'Name: value' header on req.
func applyHeaders(req *http.Request, headers []string) error {
	for _, h := range headers {
		name, value, ok := strings.Cut(h, ":")
		if !ok {
			return fmt.Errorf("invalid -H %q: want 'Name: value'", h)
		}

		req.Header.Set(strings.TrimSpace(name), strings.TrimSpace(value))
	}

	return nil
}

// writeAPIResponse writes resp to w: the status line and sorted headers
// first when includeHeaders is set (-i), then the body — pretty-printed
// when it's JSON and isTTY, verbatim otherwise (including when it isn't
// valid JSON, so a malformed body still reaches the user unmodified).
func writeAPIResponse(w io.Writer, resp *http.Response, body []byte, includeHeaders, isTTY bool) {
	if includeHeaders {
		_, _ = fmt.Fprintf(w, "%s %s\n", resp.Proto, resp.Status)

		names := make([]string, 0, len(resp.Header))
		for name := range resp.Header {
			names = append(names, name)
		}

		sort.Strings(names)

		for _, name := range names {
			for _, v := range resp.Header[name] {
				_, _ = fmt.Fprintf(w, "%s: %s\n", name, v)
			}
		}

		_, _ = fmt.Fprintln(w)
	}

	_, _ = w.Write(formatBody(body, resp.Header.Get("Content-Type"), isTTY))
}

// formatBody pretty-prints body with 2-space indentation when contentType
// is JSON and isTTY, and returns it verbatim otherwise.
func formatBody(body []byte, contentType string, isTTY bool) []byte {
	if !isTTY || !strings.Contains(contentType, "json") {
		return ensureTrailingNewline(body)
	}

	var buf bytes.Buffer
	if err := json.Indent(&buf, body, "", "  "); err != nil {
		return ensureTrailingNewline(body)
	}

	buf.WriteByte('\n')

	return buf.Bytes()
}

func ensureTrailingNewline(body []byte) []byte {
	if len(body) == 0 || body[len(body)-1] == '\n' {
		return body
	}

	return append(body, '\n')
}

// isTerminalWriter reports whether w is a terminal — false for anything
// that isn't an *os.File (a bytes.Buffer in tests included), which is
// exactly what makes the TTY decision injectable in tests: they choose
// what SetOut points at.
func isTerminalWriter(w io.Writer) bool {
	f, ok := w.(*os.File)
	if !ok {
		return false
	}

	return output.IsTerminal(f)
}

// rawResponse adapts a raw net/http response (status code + already-read
// body) to the apiResponse interface NewAPIError expects, so `feedctl api`
// can reuse the same error mapping as every command built on the generated
// client, despite not having a typed …Response value of its own.
type rawResponse struct {
	status int
	body   []byte
}

func (r rawResponse) StatusCode() int { return r.status }
func (r rawResponse) GetBody() []byte { return r.body }
