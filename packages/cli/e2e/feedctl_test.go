//go:build e2e

package e2e

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
)

// Feedctl drives a feedctl binary as a subprocess, the way a user runs it:
// with its own HOME and XDG_CONFIG_HOME, and none of the FEEDCTL_* variables
// of the shell the tests run in.
type Feedctl struct {
	t         *testing.T
	bin       string
	configDir string
	env       []string
}

// Result is one feedctl invocation's outcome.
type Result struct {
	Args     []string
	Stdout   string
	Stderr   string
	ExitCode int
}

func (r Result) String() string {
	return fmt.Sprintf("$ feedctl %s\nexit %d\n--- stdout ---\n%s\n--- stderr ---\n%s",
		strings.Join(r.Args, " "), r.ExitCode, r.Stdout, r.Stderr)
}

// BuildFeedctl builds ./cmd/feedctl into a temp directory and returns the
// binary's path. It is the real feedctl, with one file added to
// internal/config through `go build -overlay` (testdata/keyring_unavailable.go):
// it makes every OS keyring call fail as "no keyring on this platform", so
// `auth login` stores its token in hosts.yml under the test's
// XDG_CONFIG_HOME instead of the developer's keychain. On macOS go-keyring
// always shells out to /usr/bin/security, so no environment variable can
// achieve the same.
func BuildFeedctl(t *testing.T) string {
	t.Helper()

	e2eDir, err := os.Getwd()
	if err != nil {
		t.Fatalf("resolving the e2e directory: %v", err)
	}

	moduleRoot := filepath.Dir(e2eDir)
	dir := t.TempDir()

	overlay, err := json.Marshal(map[string]map[string]string{
		"Replace": {
			filepath.Join(moduleRoot, "internal", "config", "zz_e2e_keyring_unavailable.go"): filepath.Join(e2eDir, "testdata", "keyring_unavailable.go"),
		},
	})
	if err != nil {
		t.Fatalf("encoding the build overlay: %v", err)
	}

	overlayPath := filepath.Join(dir, "overlay.json")
	if err := os.WriteFile(overlayPath, overlay, 0o600); err != nil {
		t.Fatalf("writing the build overlay: %v", err)
	}

	bin := filepath.Join(dir, "feedctl")

	build := exec.Command("go", "build", "-overlay", overlayPath, "-o", bin, "./cmd/feedctl")
	build.Dir = moduleRoot

	if out, err := build.CombinedOutput(); err != nil {
		t.Fatalf("building feedctl: %v\n%s", err, out)
	}

	return bin
}

// NewFeedctl returns a runner for bin with a fresh, empty configuration.
func NewFeedctl(t *testing.T, bin string) *Feedctl {
	t.Helper()

	home := t.TempDir()
	configDir := filepath.Join(home, ".config")

	env := make([]string, 0, len(os.Environ())+2)

	for _, kv := range os.Environ() {
		name, _, _ := strings.Cut(kv, "=")
		if strings.HasPrefix(name, "FEEDCTL_") || name == "HOME" || name == "XDG_CONFIG_HOME" ||
			name == "VISUAL" || name == "EDITOR" {
			continue
		}

		env = append(env, kv)
	}

	env = append(env, "HOME="+home, "XDG_CONFIG_HOME="+configDir)

	return &Feedctl{t: t, bin: bin, configDir: configDir, env: env}
}

// HostsFile is the hosts.yml this runner's feedctl reads and writes.
func (f *Feedctl) HostsFile() string {
	return filepath.Join(f.configDir, "feedctl", "hosts.yml")
}

// Run runs feedctl with args, feeding it stdin, and returns its outcome
// whatever the exit code. It fails the test only when the process cannot be
// run at all.
func (f *Feedctl) Run(ctx context.Context, stdin string, args ...string) Result {
	f.t.Helper()

	cmd := exec.CommandContext(ctx, f.bin, args...)
	cmd.Env = f.env
	cmd.Stdin = strings.NewReader(stdin)

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err := cmd.Run()
	res := Result{Args: args, Stdout: stdout.String(), Stderr: stderr.String()}

	var exitErr *exec.ExitError

	switch {
	case err == nil:
	case errors.As(err, &exitErr):
		res.ExitCode = exitErr.ExitCode()
	default:
		f.t.Fatalf("running feedctl %s: %v", strings.Join(args, " "), err)
	}

	f.t.Logf("$ feedctl %s -> exit %d", strings.Join(args, " "), res.ExitCode)

	return res
}

// MustRun is Run, failing the test with the full outcome unless feedctl
// exits with wantExit.
func (f *Feedctl) MustRun(ctx context.Context, wantExit int, stdin string, args ...string) Result {
	f.t.Helper()

	res := f.Run(ctx, stdin, args...)
	if res.ExitCode != wantExit {
		f.t.Fatalf("want exit %d, got:\n%s", wantExit, res)
	}

	return res
}

// DecodeStdout decodes res's stdout as JSON into T, failing the test with
// the full outcome when it isn't.
func DecodeStdout[T any](t *testing.T, res Result) T {
	t.Helper()

	var v T
	if err := json.Unmarshal([]byte(res.Stdout), &v); err != nil {
		t.Fatalf("decoding stdout as JSON: %v\n%s", err, res)
	}

	return v
}

// ResponseHeader returns the value of header name in the output of
// `feedctl api -i`: a status line, one "Name: value" line per header, a
// blank line, then the body.
func ResponseHeader(res Result, name string) string {
	for _, line := range strings.Split(res.Stdout, "\n")[1:] {
		if line == "" {
			break
		}

		key, value, found := strings.Cut(line, ": ")
		if found && strings.EqualFold(key, name) {
			return value
		}
	}

	return ""
}

// StatusLine returns the status line of `feedctl api -i` output, e.g.
// "HTTP/1.1 412 Precondition Failed".
func StatusLine(res Result) string {
	line, _, _ := strings.Cut(res.Stdout, "\n")

	return line
}
