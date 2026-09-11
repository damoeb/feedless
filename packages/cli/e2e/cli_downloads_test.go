//go:build e2e

package e2e

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"testing"
)

// In SHA256SUMS order.
var feedctlDownloads = []string{
	"feedctl-darwin-amd64",
	"feedctl-darwin-arm64",
	"feedctl-linux-amd64",
	"feedctl-linux-arm64",
}

// Guards the image layout: an image with the binaries outside static/cli answered 404 on /cli/**.
func checkCLIDownloads(ctx context.Context, t *testing.T, coreURL, gatewayURL string) {
	t.Helper()

	sums := parseSHA256Sums(t, string(getCLIDownload(ctx, t, coreURL, "SHA256SUMS")))

	script := string(getCLIDownload(ctx, t, coreURL, "install.sh"))
	if want := `FEEDCTL_BASE_URL="${FEEDCTL_BASE_URL:-` + gatewayURL + `}"`; !strings.Contains(script, want) {
		t.Errorf("want /cli/install.sh templated with the core's public URL (%s), got:\n%s", want, script)
	}

	t.Run("host binary", func(t *testing.T) {
		name := fmt.Sprintf("feedctl-%s-%s", runtime.GOOS, runtime.GOARCH)

		wantSum, ok := sums[name]
		if !ok {
			t.Skipf("the core ships no feedctl build for this host (%s)", name)
		}

		binary := getCLIDownload(ctx, t, coreURL, name)
		if sum := sha256.Sum256(binary); hex.EncodeToString(sum[:]) != wantSum {
			t.Fatalf("/cli/%s has SHA-256 %x, but SHA256SUMS says %s", name, sum, wantSum)
		}

		path := filepath.Join(t.TempDir(), name)
		if err := os.WriteFile(path, binary, 0o755); err != nil {
			t.Fatalf("writing %s: %v", path, err)
		}

		out, err := exec.CommandContext(ctx, path, "--version").CombinedOutput()
		if err != nil {
			t.Fatalf("%s --version: %v\n%s", name, err, out)
		}

		// The image embeds its APP_VERSION, which the core also reports as its version.
		want := "feedctl version " + serverVersion(ctx, t, coreURL)
		if got := strings.TrimSpace(string(out)); got != want {
			t.Fatalf("want the downloaded %s to print %q, got %q", name, want, got)
		}
	})
}

func parseSHA256Sums(t *testing.T, sums string) map[string]string {
	t.Helper()

	lines := strings.Split(strings.TrimRight(sums, "\n"), "\n")
	if len(lines) != len(feedctlDownloads) {
		t.Fatalf("want /cli/SHA256SUMS to list %d files, got:\n%s", len(feedctlDownloads), sums)
	}

	digests := make(map[string]string, len(lines))

	for i, line := range lines {
		digest, file, ok := strings.Cut(line, "  ")
		if !ok || len(digest) != sha256.Size*2 || file != feedctlDownloads[i] {
			t.Fatalf("want line %d of /cli/SHA256SUMS to be `<sha256>  %s`, got %q", i+1, feedctlDownloads[i], line)
		}

		digests[file] = digest
	}

	return digests
}

func getCLIDownload(ctx context.Context, t *testing.T, coreURL, name string) []byte {
	t.Helper()

	body, status := httpGet(ctx, t, coreURL+"/cli/"+name)
	if status != http.StatusOK {
		t.Fatalf("GET /cli/%s without a token: want 200, got %d: %.200s", name, status, body)
	}

	return body
}

func serverVersion(ctx context.Context, t *testing.T, coreURL string) string {
	t.Helper()

	body, status := httpGet(ctx, t, coreURL+"/api/v1/status")
	if status != http.StatusOK {
		t.Fatalf("GET /api/v1/status: want 200, got %d: %.200s", status, body)
	}

	var serverStatus struct {
		Version string `json:"version"`
	}
	if err := json.Unmarshal(body, &serverStatus); err != nil || serverStatus.Version == "" {
		t.Fatalf("want a version in the /api/v1/status response (%v), got: %.200s", err, body)
	}

	return serverStatus.Version
}

func httpGet(ctx context.Context, t *testing.T, url string) (body []byte, status int) {
	t.Helper()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		t.Fatalf("building GET %s: %v", url, err)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("GET %s: %v", url, err)
	}
	defer func() { _ = resp.Body.Close() }()

	body, err = io.ReadAll(resp.Body)
	if err != nil {
		t.Fatalf("reading GET %s: %v", url, err)
	}

	return body, resp.StatusCode
}
