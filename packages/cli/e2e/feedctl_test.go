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

// Feedctl runs with its own HOME and none of the shell's FEEDCTL_* variables.
type Feedctl struct {
	t         *testing.T
	bin       string
	configDir string
	env       []string
}

type Result struct {
	Args     []string
	Stdout   string
	Stderr   string
	ExitCode int
}

// String renders r for a failure message, with credentials redacted.
func (r Result) String() string {
	return Redact(fmt.Sprintf("$ feedctl %s\nexit %d\n--- stdout ---\n%s\n--- stderr ---\n%s",
		strings.Join(r.Args, " "), r.ExitCode, r.Stdout, r.Stderr))
}

// BuildFeedctl overlays testdata/keyring_unavailable.go so tokens land in hosts.yml, not the developer's keychain;
// on macOS go-keyring always shells out to /usr/bin/security, so no environment variable can do this.
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

func (f *Feedctl) HostsFile() string {
	return filepath.Join(f.configDir, "feedctl", "hosts.yml")
}

// Run fails the test only when the process can't run at all.
func (f *Feedctl) Run(ctx context.Context, stdin string, args ...string) Result {
	f.t.Helper()

	res := f.RunQuiet(ctx, stdin, args...)

	f.t.Logf("%s", Redact(fmt.Sprintf("$ feedctl %s -> exit %d%s%s", strings.Join(args, " "), res.ExitCode,
		indentedTail("stdout", res.Stdout), indentedTail("stderr", res.Stderr))))

	return res
}

// RunQuiet skips the log echo, which would flood polling loops.
func (f *Feedctl) RunQuiet(ctx context.Context, stdin string, args ...string) Result {
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

	return res
}

const outputTailLines = 15

func indentedTail(label, out string) string {
	out = strings.TrimRight(out, "\n")
	if out == "" {
		return ""
	}

	lines := strings.Split(out, "\n")
	if len(lines) > outputTailLines {
		lines = append([]string{fmt.Sprintf("… (%d earlier lines)", len(lines)-outputTailLines)}, lines[len(lines)-outputTailLines:]...)
	}

	return "\n  " + label + ":\n    " + strings.Join(lines, "\n    ")
}

func (f *Feedctl) MustRun(ctx context.Context, wantExit int, stdin string, args ...string) Result {
	f.t.Helper()

	res := f.Run(ctx, stdin, args...)
	if res.ExitCode != wantExit {
		f.t.Fatalf("want exit %d, got:\n%s", wantExit, res)
	}

	return res
}

func DecodeStdout[T any](t *testing.T, res Result) T {
	t.Helper()

	var v T
	if err := json.Unmarshal([]byte(res.Stdout), &v); err != nil {
		t.Fatalf("decoding stdout as JSON: %v\n%s", err, res)
	}

	return v
}

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

func StatusLine(res Result) string {
	line, _, _ := strings.Cut(res.Stdout, "\n")

	return line
}
