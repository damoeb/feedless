// Package config owns hosts.yml, the OS keyring and host/token resolution.
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

	// Enforced on every save, whatever the umask: hosts.yml can hold a plain-text token.
	dirPerm  os.FileMode = 0o700
	filePerm os.FileMode = 0o600
)

type HostEntry struct {
	URL  string `yaml:"url"`
	User string `yaml:"user"`
	// Set only when the OS keyring was unavailable; see StoreToken.
	Token string `yaml:"token,omitempty"`
}

type Config struct {
	DefaultHost string               `yaml:"default_host,omitempty"`
	Hosts       map[string]HostEntry `yaml:"hosts,omitempty"`
}

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

func Path() (string, error) {
	dir, err := Dir()
	if err != nil {
		return "", err
	}

	return filepath.Join(dir, fileName), nil
}

// A missing file loads as an empty Config.
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
