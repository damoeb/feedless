// Package config owns feedctl's per-host configuration: the hosts.yml file
// (host URLs, users, and — only when no OS keyring is available — tokens
// stored in plain text), the OS keyring, and the host/token resolution
// order every command follows. internal/client builds on top of it to
// construct an authenticated API client; see client.NewFromConfig.
package config

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"gopkg.in/yaml.v3"
)

const (
	appDirName = "feedctl"
	fileName   = "hosts.yml"

	// dirPerm and filePerm are enforced on every save, regardless of the
	// process umask or a pre-existing file/directory with looser
	// permissions: hosts.yml can hold a plain-text token.
	dirPerm  os.FileMode = 0o700
	filePerm os.FileMode = 0o600
)

// HostEntry is one configured host's record in hosts.yml.
type HostEntry struct {
	URL  string `yaml:"url"`
	User string `yaml:"user"`
	// Token is only ever populated when the token could not be stored in
	// the OS keyring; see StoreToken. Most host entries omit it entirely.
	Token string `yaml:"token,omitempty"`
}

// Config is the parsed content of hosts.yml.
type Config struct {
	DefaultHost string               `yaml:"default_host,omitempty"`
	Hosts       map[string]HostEntry `yaml:"hosts,omitempty"`
}

// Dir returns the directory hosts.yml lives in: $XDG_CONFIG_HOME/feedctl,
// falling back to ~/.config/feedctl.
func Dir() (string, error) {
	if xdg := os.Getenv("XDG_CONFIG_HOME"); xdg != "" {
		return filepath.Join(xdg, appDirName), nil
	}

	home, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("resolving home directory: %w", err)
	}

	return filepath.Join(home, ".config", appDirName), nil
}

// Path returns the full path to hosts.yml.
func Path() (string, error) {
	dir, err := Dir()
	if err != nil {
		return "", err
	}

	return filepath.Join(dir, fileName), nil
}

// Load reads hosts.yml. A missing file is not an error: it returns an
// empty Config, as if no host had ever been configured.
func Load() (*Config, error) {
	path, err := Path()
	if err != nil {
		return nil, err
	}

	data, err := os.ReadFile(path)
	if errors.Is(err, os.ErrNotExist) {
		return &Config{Hosts: map[string]HostEntry{}}, nil
	}
	if err != nil {
		return nil, fmt.Errorf("reading %s: %w", path, err)
	}

	cfg := &Config{}
	if err := yaml.Unmarshal(data, cfg); err != nil {
		return nil, fmt.Errorf("parsing %s: %w", path, err)
	}

	if cfg.Hosts == nil {
		cfg.Hosts = map[string]HostEntry{}
	}

	return cfg, nil
}

// Save writes hosts.yml, creating its directory if necessary. The
// directory is always left at 0700 and the file at 0600, even if either
// already existed with looser permissions.
func (c *Config) Save() error {
	dir, err := Dir()
	if err != nil {
		return err
	}

	if err := os.MkdirAll(dir, dirPerm); err != nil {
		return fmt.Errorf("creating %s: %w", dir, err)
	}
	if err := os.Chmod(dir, dirPerm); err != nil {
		return fmt.Errorf("setting permissions on %s: %w", dir, err)
	}

	path, err := Path()
	if err != nil {
		return err
	}

	data, err := yaml.Marshal(c)
	if err != nil {
		return fmt.Errorf("encoding %s: %w", path, err)
	}

	if err := os.WriteFile(path, data, filePerm); err != nil {
		return fmt.Errorf("writing %s: %w", path, err)
	}

	if err := os.Chmod(path, filePerm); err != nil {
		return fmt.Errorf("setting permissions on %s: %w", path, err)
	}

	return nil
}
