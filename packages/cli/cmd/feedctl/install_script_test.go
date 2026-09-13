package main

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"testing"
)

// CliInstallScriptController (server-core) replaces every occurrence.
const installScriptPlaceholder = "__FEEDCTL_BASE_URL__"

const fakeFeedctl = "#!/bin/sh\necho \"feedctl version test\"\n"

func TestInstallScriptTemplatedByTheServerInstalls(t *testing.T) {
	requireInstallScriptTools(t)

	binary, ok := hostBinaryName()
	if !ok {
		t.Skipf("install.sh has no build for %s/%s", runtime.GOOS, runtime.GOARCH)
	}

	sum := sha256.Sum256([]byte(fakeFeedctl))
	sums := fmt.Sprintf("%s  %s\n", hex.EncodeToString(sum[:]), binary)

	mux := http.NewServeMux()
	mux.HandleFunc("/cli/"+binary, func(w http.ResponseWriter, _ *http.Request) {
		_, _ = w.Write([]byte(fakeFeedctl))
	})
	mux.HandleFunc("/cli/SHA256SUMS", func(w http.ResponseWriter, _ *http.Request) {
		_, _ = w.Write([]byte(sums))
	})

	instance := httptest.NewServer(mux)
	defer instance.Close()

	script := strings.ReplaceAll(readInstallScript(t), installScriptPlaceholder, instance.URL)
	installDir := t.TempDir()

	stdout, stderr, err := runInstallScript(t, script, "--dir", installDir)
	if err != nil {
		t.Fatalf("templated install.sh: want exit 0, got %v\nstdout:\n%s\nstderr:\n%s", err, stdout, stderr)
	}

	if !strings.Contains(stdout, "feedctl version test") {
		t.Errorf("want install.sh to run the installed feedctl --version, got stdout:\n%s", stdout)
	}

	installed := filepath.Join(installDir, "feedctl")

	out, err := exec.Command(installed, "--version").CombinedOutput()
	if err != nil {
		t.Fatalf("running the installed %s: %v\n%s", installed, err, out)
	}

	if got := strings.TrimSpace(string(out)); got != "feedctl version test" {
		t.Errorf("want the installed feedctl to print %q, got %q", "feedctl version test", got)
	}
}

func TestInstallScriptUntemplatedRefusesToRun(t *testing.T) {
	requireInstallScriptTools(t)

	_, stderr, err := runInstallScript(t, readInstallScript(t), "--dir", t.TempDir())
	if err == nil {
		t.Fatalf("untemplated install.sh: want a non-zero exit, got 0\nstderr:\n%s", stderr)
	}

	if !strings.Contains(stderr, "FEEDCTL_BASE_URL is not set") {
		t.Errorf("want the \"not set\" message, got stderr:\n%s", stderr)
	}
}

func requireInstallScriptTools(t *testing.T) {
	t.Helper()

	for _, tool := range []string{"sh", "curl"} {
		if _, err := exec.LookPath(tool); err != nil {
			t.Skipf("install.sh needs %s: %v", tool, err)
		}
	}
}

func hostBinaryName() (string, bool) {
	switch runtime.GOOS + "/" + runtime.GOARCH {
	case "darwin/amd64", "darwin/arm64", "linux/amd64", "linux/arm64":
		return "feedctl-" + runtime.GOOS + "-" + runtime.GOARCH, true
	default:
		return "", false
	}
}

func readInstallScript(t *testing.T) string {
	t.Helper()

	script, err := os.ReadFile(filepath.Join("..", "..", "install.sh"))
	if err != nil {
		t.Fatalf("reading install.sh: %v", err)
	}

	return string(script)
}

// Without FEEDCTL_BASE_URL, so the script relies on its templated default.
func runInstallScript(t *testing.T, script string, args ...string) (stdout, stderr string, err error) {
	t.Helper()

	path := filepath.Join(t.TempDir(), "install.sh")
	if err := os.WriteFile(path, []byte(script), 0o600); err != nil {
		t.Fatalf("writing %s: %v", path, err)
	}

	var env []string

	for _, kv := range os.Environ() {
		if !strings.HasPrefix(kv, "FEEDCTL_BASE_URL=") {
			env = append(env, kv)
		}
	}

	var out, errOut bytes.Buffer

	cmd := exec.Command("sh", append([]string{path}, args...)...)
	cmd.Env = env
	cmd.Stdout = &out
	cmd.Stderr = &errOut
	err = cmd.Run()

	return out.String(), errOut.String(), err
}
