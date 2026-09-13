package config

import (
	"os"
	"path/filepath"
	"runtime"
	"testing"
)

// Keeps Load/Save off the real ~/.config/feedctl.
func withTempConfigHome(t *testing.T) string {
	t.Helper()

	dir := t.TempDir()
	t.Setenv("XDG_CONFIG_HOME", dir)

	return dir
}

func TestDir_UsesXDGConfigHome(t *testing.T) {
	xdg := withTempConfigHome(t)

	dir, err := Dir()
	if err != nil {
		t.Fatalf("Dir() error = %v", err)
	}

	want := filepath.Join(xdg, "feedctl")
	if dir != want {
		t.Errorf("Dir() = %q, want %q", dir, want)
	}
}

func TestDir_FallsBackToHomeConfig(t *testing.T) {
	t.Setenv("XDG_CONFIG_HOME", "")
	home := t.TempDir()
	t.Setenv("HOME", home)
	if runtime.GOOS == "windows" {
		t.Skip("HOME fallback path differs on windows")
	}

	dir, err := Dir()
	if err != nil {
		t.Fatalf("Dir() error = %v", err)
	}

	want := filepath.Join(home, ".config", "feedctl")
	if dir != want {
		t.Errorf("Dir() = %q, want %q", dir, want)
	}
}

func TestLoad_MissingFile_ReturnsEmptyConfig(t *testing.T) {
	withTempConfigHome(t)

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if cfg.DefaultHost != "" {
		t.Errorf("DefaultHost = %q, want empty", cfg.DefaultHost)
	}
	if len(cfg.Hosts) != 0 {
		t.Errorf("Hosts = %v, want empty", cfg.Hosts)
	}
}

func TestSaveThenLoad_RoundTrips(t *testing.T) {
	withTempConfigHome(t)

	cfg := &Config{
		DefaultHost: "feedless.example.org",
		Hosts: map[string]HostEntry{
			"feedless.example.org": {URL: "https://feedless.example.org", User: "someone@example.org"},
		},
	}

	if err := cfg.Save(); err != nil {
		t.Fatalf("Save() error = %v", err)
	}

	got, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if got.DefaultHost != cfg.DefaultHost {
		t.Errorf("DefaultHost = %q, want %q", got.DefaultHost, cfg.DefaultHost)
	}

	entry, ok := got.Hosts["feedless.example.org"]
	if !ok {
		t.Fatalf("Hosts[feedless.example.org] missing, got %v", got.Hosts)
	}
	if entry.URL != "https://feedless.example.org" || entry.User != "someone@example.org" {
		t.Errorf("Hosts[feedless.example.org] = %+v, want URL/User set", entry)
	}
}

func TestSave_SetsRestrictivePermissions(t *testing.T) {
	if runtime.GOOS == "windows" {
		t.Skip("POSIX permission bits don't apply on windows")
	}

	xdg := withTempConfigHome(t)

	cfg := &Config{Hosts: map[string]HostEntry{}}
	if err := cfg.Save(); err != nil {
		t.Fatalf("Save() error = %v", err)
	}

	dirInfo, err := os.Stat(filepath.Join(xdg, "feedctl"))
	if err != nil {
		t.Fatalf("stat config dir: %v", err)
	}
	if perm := dirInfo.Mode().Perm(); perm != 0o700 {
		t.Errorf("config dir mode = %o, want 0700", perm)
	}

	path, err := Path()
	if err != nil {
		t.Fatalf("Path() error = %v", err)
	}
	fileInfo, err := os.Stat(path)
	if err != nil {
		t.Fatalf("stat hosts.yml: %v", err)
	}
	if perm := fileInfo.Mode().Perm(); perm != 0o600 {
		t.Errorf("hosts.yml mode = %o, want 0600", perm)
	}
}

func TestSave_TightensPreExistingLoosePermissions(t *testing.T) {
	if runtime.GOOS == "windows" {
		t.Skip("POSIX permission bits don't apply on windows")
	}

	xdg := withTempConfigHome(t)
	dir := filepath.Join(xdg, "feedctl")
	if err := os.MkdirAll(dir, 0o755); err != nil {
		t.Fatalf("pre-creating config dir: %v", err)
	}
	path := filepath.Join(dir, "hosts.yml")
	if err := os.WriteFile(path, []byte("default_host: \"\"\n"), 0o644); err != nil {
		t.Fatalf("pre-creating hosts.yml: %v", err)
	}

	cfg := &Config{Hosts: map[string]HostEntry{}}
	if err := cfg.Save(); err != nil {
		t.Fatalf("Save() error = %v", err)
	}

	dirInfo, err := os.Stat(dir)
	if err != nil {
		t.Fatalf("stat config dir: %v", err)
	}
	if perm := dirInfo.Mode().Perm(); perm != 0o700 {
		t.Errorf("config dir mode = %o, want 0700", perm)
	}

	fileInfo, err := os.Stat(path)
	if err != nil {
		t.Fatalf("stat hosts.yml: %v", err)
	}
	if perm := fileInfo.Mode().Perm(); perm != 0o600 {
		t.Errorf("hosts.yml mode = %o, want 0600", perm)
	}
}
