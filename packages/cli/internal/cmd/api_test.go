package cmd

import (
	"encoding/json"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"strings"
	"testing"

	"github.com/damoeb/feedless/packages/cli/internal/config"
)

// setupLoggedInHost writes hosts.yml and stores token in the (mocked)
// keyring for srvURL's host, as if `auth login` had already run, without
// needing srv to serve /user — so tests can point their own httptest
// handler at whatever path `feedctl api` targets. It returns the host name
// (hostOf(srvURL)) for callers that need it, and makes it the default host,
// so `feedctl api <path>` needs no --host.
func setupLoggedInHost(t *testing.T, srvURL, token string) string {
	t.Helper()
	withTempConfigHome(t)

	host := hostOf(srvURL)
	if _, _, err := config.StoreToken(host, token, io.Discard); err != nil {
		t.Fatalf("StoreToken() error = %v", err)
	}

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}

	cfg.Hosts[host] = config.HostEntry{URL: srvURL, User: "tester@example.org"}
	cfg.DefaultHost = host

	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	return host
}

func TestAPI_AbsoluteURL_Refused(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("api", "https://evil.example.com/x")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error for an absolute URL")
	}
	if !strings.Contains(err.Error(), "absolute") {
		t.Errorf("error = %v, want it to mention 'absolute'", err)
	}
}

func TestAPI_FieldsAndInput_MutuallyExclusive(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("api", "repositories", "-f", "a=b", "--input", "-")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error")
	}
	if !strings.Contains(err.Error(), "mutually exclusive") {
		t.Errorf("error = %v, want it to mention 'mutually exclusive'", err)
	}
}

func TestAPI_DefaultMethod_GET_NoBody(t *testing.T) {
	var gotMethod string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotMethod = r.Method
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"ok":true}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("api", "repositories")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotMethod != http.MethodGet {
		t.Errorf("method = %s, want GET", gotMethod)
	}
	if !strings.Contains(stdout.String(), `"ok"`) {
		t.Errorf("stdout = %q, want the response body", stdout.String())
	}
}

func TestAPI_GET_FieldsBecomeQueryParams(t *testing.T) {
	var gotQuery url.Values

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotQuery = r.URL.Query()
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"items":[]}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	// -X GET is explicit here: -f alone (no --method) defaults to POST (see
	// TestAPI_FieldsWithoutMethodFlag_DefaultToPOSTJSONBody) — it's the
	// *effective* method, GET either way, that routes -f to the query
	// string instead of a JSON body.
	_, stderr, err := runCmd("api", "repositories", "-X", "GET", "-f", "q=hello", "-f", "page=2")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotQuery.Get("q") != "hello" || gotQuery.Get("page") != "2" {
		t.Errorf("query = %v, want q=hello and page=2", gotQuery)
	}
}

func TestAPI_FieldsWithoutMethodFlag_DefaultToPOSTJSONBody(t *testing.T) {
	var gotMethod, gotContentType string
	var gotBody []byte

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotMethod = r.Method
		gotContentType = r.Header.Get("Content-Type")
		gotBody, _ = io.ReadAll(r.Body)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		_, _ = w.Write([]byte(`{"id":"1"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("api", "repositories", "-f", "title=Hello")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotMethod != http.MethodPost {
		t.Errorf("method = %s, want POST", gotMethod)
	}
	if gotContentType != "application/json" {
		t.Errorf("Content-Type = %q, want application/json", gotContentType)
	}

	var body map[string]string
	if err := json.Unmarshal(gotBody, &body); err != nil {
		t.Fatalf("request body isn't valid JSON: %v (%s)", err, gotBody)
	}
	if body["title"] != "Hello" {
		t.Errorf("body = %v, want title=Hello", body)
	}
}

func TestAPI_FieldWithAtPrefix_ReadsFile(t *testing.T) {
	dir := t.TempDir()
	path := dir + "/value.txt"
	if err := os.WriteFile(path, []byte("file content\n"), 0o600); err != nil {
		t.Fatalf("WriteFile() error = %v", err)
	}

	var gotBody []byte

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotBody, _ = io.ReadAll(r.Body)
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("api", "repositories", "-f", "body=@"+path)
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	var body map[string]string
	if err := json.Unmarshal(gotBody, &body); err != nil {
		t.Fatalf("request body isn't valid JSON: %v (%s)", err, gotBody)
	}
	if body["body"] != "file content" {
		t.Errorf("body[body] = %q, want %q", body["body"], "file content")
	}
}

func TestAPI_Input_Stdin_SendsBodyVerbatimAndDefaultsToPOST(t *testing.T) {
	var gotMethod string
	var gotBody []byte

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotMethod = r.Method
		gotBody, _ = io.ReadAll(r.Body)
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmdWithStdin(`{"raw":true}`, "api", "repositories", "--input", "-")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotMethod != http.MethodPost {
		t.Errorf("method = %s, want POST", gotMethod)
	}
	if string(gotBody) != `{"raw":true}` {
		t.Errorf("body = %s, want the stdin content verbatim", gotBody)
	}
}

func TestAPI_ExplicitMethodFlag_Wins(t *testing.T) {
	var gotMethod string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotMethod = r.Method
		w.WriteHeader(http.StatusNoContent)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("api", "repositories/abc", "-X", "DELETE")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotMethod != http.MethodDelete {
		t.Errorf("method = %s, want DELETE", gotMethod)
	}
}

func TestAPI_Include_PrintsStatusLineAndHeadersBeforeBody(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("X-Custom", "value")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("plain body"))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("api", "ping", "-i")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	out := stdout.String()
	if !strings.Contains(out, "200") {
		t.Errorf("stdout = %q, want the status line to mention 200", out)
	}
	if !strings.Contains(out, "X-Custom: value") {
		t.Errorf("stdout = %q, want the X-Custom header", out)
	}
	if !strings.HasSuffix(out, "plain body\n") {
		t.Errorf("stdout = %q, want it to end with the body", out)
	}
}

func TestAPI_401_ExitsWithCode4(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusUnauthorized)
		_, _ = w.Write([]byte(`{"code":"UNAUTHORIZED","message":"invalid token"}`))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	// root.SilenceErrors is true (see root.go), so Execute() only returns
	// the error — main.go's run() is what prints it to stderr (see
	// TestAPIError_RenderError_PrintsMessageAndFieldErrors for that
	// rendering, and cmd/feedctl/main_test.go for the equivalent full
	// stderr assertion on a different error type). Here it's enough to
	// check the error itself carries the right exit code and message.
	_, _, err := runCmd("api", "repositories")
	if err == nil {
		t.Fatal("Execute() error = nil, want an error for a 401 response")
	}

	assertExitCode(t, err, 4)

	if !strings.Contains(err.Error(), "invalid token") {
		t.Errorf("error = %v, want it to contain the server's message", err)
	}
}

func TestAPI_NonJSONBody_PrintedVerbatim(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/plain")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("hello"))
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("api", "ping")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if stdout.String() != "hello\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "hello\n")
	}
}

func TestAPI_Headers_AreSentOnRequest(t *testing.T) {
	var gotHeader string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotHeader = r.Header.Get("X-Trace")
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("api", "ping", "-H", "X-Trace: abc123")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotHeader != "abc123" {
		t.Errorf("X-Trace = %q, want %q", gotHeader, "abc123")
	}
}

func TestFormatBody_JSON_TTY_PrettyPrints(t *testing.T) {
	got := string(formatBody([]byte(`{"a":1,"b":2}`), "application/json; charset=utf-8", true))

	want := "{\n  \"a\": 1,\n  \"b\": 2\n}\n"
	if got != want {
		t.Errorf("formatBody() = %q, want %q", got, want)
	}
}

func TestFormatBody_JSON_NonTTY_PrintsVerbatim(t *testing.T) {
	body := []byte(`{"a":1,"b":2}`)

	got := string(formatBody(body, "application/json", false))

	want := string(body) + "\n"
	if got != want {
		t.Errorf("formatBody() = %q, want %q (verbatim + trailing newline)", got, want)
	}
}

func TestFormatBody_NonJSON_TTY_PrintsVerbatim(t *testing.T) {
	got := string(formatBody([]byte("plain text"), "text/plain", true))

	if got != "plain text\n" {
		t.Errorf("formatBody() = %q, want %q", got, "plain text\n")
	}
}

func TestFormatBody_MalformedJSON_TTY_FallsBackToVerbatim(t *testing.T) {
	got := string(formatBody([]byte(`{not json`), "application/json", true))

	if got != "{not json\n" {
		t.Errorf("formatBody() = %q, want the malformed body unchanged plus a trailing newline", got)
	}
}

func TestAPI_LeadingSlashOptional(t *testing.T) {
	var gotPath string

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.WriteHeader(http.StatusOK)
	}))
	t.Cleanup(srv.Close)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("api", "repositories")
	if err != nil {
		t.Fatalf("Execute() error = %v, stderr = %q", err, stderr.String())
	}

	if gotPath != "/api/v1/repositories" {
		t.Errorf("path = %q, want /api/v1/repositories", gotPath)
	}
}
