package cmd

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/damoeb/feedless/packages/cli/internal/config"
)

// statusBody is a GET /api/v1/status response; 1757000000000 is
// 2025-09-04 15:33:20 UTC.
func statusBody(version string, connected int) string {
	v, _ := json.Marshal(version)

	return `{"version":` + string(v) + `,"build":{"commit":"abc123","date":1757000000000},"agents":{"connected":` + itoa(connected) + `}}`
}

// statusServer answers GET /api/v1/status with status and body, stamping
// serverVersion as X-Feedless-Version. It records the Authorization header
// of the last request into *gotAuth when gotAuth is non-nil.
func statusServer(t *testing.T, status int, body, serverVersion string, gotAuth *string) *httptest.Server {
	t.Helper()

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet || r.URL.Path != "/api/v1/status" {
			t.Errorf("request = %s %s, want GET /api/v1/status", r.Method, r.URL.Path)
		}

		if gotAuth != nil {
			*gotAuth = r.Header.Get("Authorization")
		}

		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("X-Feedless-Version", serverVersion)
		w.WriteHeader(status)
		_, _ = w.Write([]byte(body))
	}))
	t.Cleanup(srv.Close)

	return srv
}

// exitCodeOf mirrors main.go's exitCodeFor: 0 for no error, the error's
// ExitCode() when it has one, else 1.
func exitCodeOf(err error) int {
	if err == nil {
		return 0
	}

	var ec interface{ ExitCode() int }
	if errors.As(err, &ec) {
		return ec.ExitCode()
	}

	return 1
}

func TestStatus_AgentConnected_PrintsSummaryAndExits0(t *testing.T) {
	var gotAuth string

	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 2), "1.0.0", &gotAuth)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, stderr, err := runCmd("status")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0; stderr = %q", exitCodeOf(err), err, stderr.String())
	}

	want := "Server:   " + srv.URL + "\n" +
		"Version:  1.0.0 (commit abc123, built 2025-09-04 15:33 UTC)\n" +
		"CLI:      1.0.0\n" +
		"Agents:   2 connected\n"
	if stdout.String() != want {
		t.Errorf("stdout = %q, want %q", stdout.String(), want)
	}

	if stderr.String() != "" {
		t.Errorf("stderr = %q, want nothing", stderr.String())
	}

	if gotAuth != "" {
		t.Errorf("Authorization = %q, want none: status is public", gotAuth)
	}
}

func TestStatus_NoAgentConnected_PrintsSummaryThenExits1(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 0), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status")
	if exitCodeOf(err) != 1 {
		t.Fatalf("exit = %d (err %v), want 1", exitCodeOf(err), err)
	}

	if err.Error() != "no prerender agent connected" {
		t.Errorf("error = %q, want %q", err.Error(), "no prerender agent connected")
	}

	if !strings.Contains(stdout.String(), "Agents:   0 connected\n") {
		t.Errorf("stdout = %q, want the summary printed before failing", stdout.String())
	}
}

func TestStatus_ServerError_Exits1(t *testing.T) {
	srv := statusServer(t, http.StatusInternalServerError, `{"code":"INTERNAL_ERROR","message":"unexpected error"}`, "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status")
	if exitCodeOf(err) != 1 {
		t.Fatalf("exit = %d (err %v), want 1", exitCodeOf(err), err)
	}

	if !strings.Contains(err.Error(), "unexpected error") {
		t.Errorf("error = %q, want the server's message", err.Error())
	}

	if stdout.String() != "" {
		t.Errorf("stdout = %q, want nothing", stdout.String())
	}
}

// An instance older than GET /status answers 401 (every other /api/v1 path
// needs a token). That is not something `feedctl auth login` fixes, so it
// exits 1, not 4.
func TestStatus_Unauthorized_Exits1Not4(t *testing.T) {
	srv := statusServer(t, http.StatusUnauthorized, `{"code":"UNAUTHORIZED","message":"Authentication required"}`, "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	_, _, err := runCmd("status")
	if exitCodeOf(err) != 1 {
		t.Fatalf("exit = %d (err %v), want 1", exitCodeOf(err), err)
	}
}

func TestStatus_Unreachable_Exits1(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 1), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")
	srv.Close()

	_, _, err := runCmd("status")
	if exitCodeOf(err) != 1 {
		t.Fatalf("exit = %d (err %v), want 1", exitCodeOf(err), err)
	}

	if !strings.Contains(err.Error(), "contacting") {
		t.Errorf("error = %q, want it to say the host could not be contacted", err.Error())
	}
}

func TestStatus_NoHost_Exits2(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("status")
	if exitCodeOf(err) != 2 {
		t.Fatalf("exit = %d (err %v), want 2", exitCodeOf(err), err)
	}

	if !strings.Contains(err.Error(), "--host") {
		t.Errorf("error = %q, want it to point at --host", err.Error())
	}
}

func TestStatus_HostURLWithoutLogin_Exits0(t *testing.T) {
	withTempConfigHome(t)

	var gotAuth string

	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 1), "1.0.0", &gotAuth)

	stdout, stderr, err := runCmd("status", "--host", srv.URL)
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0; stderr = %q", exitCodeOf(err), err, stderr.String())
	}

	if !strings.HasPrefix(stdout.String(), "Server:   "+srv.URL+"\n") {
		t.Errorf("stdout = %q, want the --host URL as the server", stdout.String())
	}

	if gotAuth != "" {
		t.Errorf("Authorization = %q, want none", gotAuth)
	}
}

func TestStatus_OnlyConfiguredHostWithoutDefault_IsUsed(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 1), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("config.Load() error = %v", err)
	}

	cfg.DefaultHost = ""
	if err := cfg.Save(); err != nil {
		t.Fatalf("cfg.Save() error = %v", err)
	}

	_, stderr, err := runCmd("status")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0; stderr = %q", exitCodeOf(err), err, stderr.String())
	}
}

func TestStatus_JSON_PrintsTheServerResponse(t *testing.T) {
	body := statusBody("1.0.0", 2)
	srv := statusServer(t, http.StatusOK, body, "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status", "--json")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0", exitCodeOf(err), err)
	}

	var got, want any
	if err := json.Unmarshal(stdout.Bytes(), &got); err != nil {
		t.Fatalf("stdout is not JSON: %v\n%s", err, stdout.String())
	}

	_ = json.Unmarshal([]byte(body), &want)

	gotJSON, _ := json.Marshal(got)
	wantJSON, _ := json.Marshal(want)

	if string(gotJSON) != string(wantJSON) {
		t.Errorf("stdout = %s, want the server's response %s", gotJSON, wantJSON)
	}
}

func TestStatus_JSON_NoAgentConnected_StillExits1(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 0), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status", "--json")
	if exitCodeOf(err) != 1 {
		t.Fatalf("exit = %d (err %v), want 1", exitCodeOf(err), err)
	}

	if !strings.Contains(stdout.String(), `"connected": 0`) {
		t.Errorf("stdout = %q, want the response printed before failing", stdout.String())
	}
}

func TestStatus_JQ_FiltersTheResponse(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0", 2), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status", "--json", "--jq", ".agents.connected")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0", exitCodeOf(err), err)
	}

	if stdout.String() != "2\n" {
		t.Errorf("stdout = %q, want %q", stdout.String(), "2\n")
	}
}

func TestStatus_JQWithoutJSON_Errors(t *testing.T) {
	withTempConfigHome(t)

	_, _, err := runCmd("status", "--jq", ".version")
	if err == nil || !strings.Contains(err.Error(), "--jq requires --json") {
		t.Errorf("error = %v, want --jq requires --json", err)
	}
}

func TestStatus_VersionMismatch_WarnsOnStderr(t *testing.T) {
	srv := statusServer(t, http.StatusOK, statusBody("0.9.0", 1), "0.9.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	_, stderr, err := runCmd("status")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0", exitCodeOf(err), err)
	}

	if !strings.Contains(stderr.String(), "warning: feedctl 1.0.0 differs from") {
		t.Errorf("stderr = %q, want the version-mismatch warning", stderr.String())
	}
}

func TestStatus_ServerTextIsSanitized(t *testing.T) {
	esc := string(rune(0x1b))
	srv := statusServer(t, http.StatusOK, statusBody("1.0.0"+esc+"]0;pwned"+string(rune(0x07)), 1), "1.0.0", nil)
	setupLoggedInHost(t, srv.URL, "tok")

	stdout, _, err := runCmd("status")
	if exitCodeOf(err) != 0 {
		t.Fatalf("exit = %d (err %v), want 0", exitCodeOf(err), err)
	}

	if strings.ContainsRune(stdout.String(), 0x1b) || strings.ContainsRune(stdout.String(), 0x07) {
		t.Errorf("stdout = %q, want no control sequences", stdout.String())
	}
}
