package cmd

import (
	"testing"

	"github.com/spf13/cobra"
)

func newRepoTestCmd(t *testing.T) *cobra.Command {
	t.Helper()

	c := &cobra.Command{Use: "x"}
	addRepoFlag(c)

	return c
}

func TestResolveRepo_FlagWins(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "env-repo")

	c := newRepoTestCmd(t)
	if err := c.Flags().Set("repo", "flag-repo"); err != nil {
		t.Fatalf("Set(--repo) error = %v", err)
	}

	got, err := resolveRepo(c, true)
	if err != nil {
		t.Fatalf("resolveRepo() error = %v", err)
	}
	if got != "flag-repo" {
		t.Errorf("resolveRepo() = %q, want %q", got, "flag-repo")
	}
}

func TestResolveRepo_FallsBackToEnv(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "env-repo")

	c := newRepoTestCmd(t)

	got, err := resolveRepo(c, true)
	if err != nil {
		t.Fatalf("resolveRepo() error = %v", err)
	}
	if got != "env-repo" {
		t.Errorf("resolveRepo() = %q, want %q", got, "env-repo")
	}
}

func TestResolveRepo_Required_NeitherSet_Errors(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "")

	c := newRepoTestCmd(t)

	_, err := resolveRepo(c, true)
	if err == nil {
		t.Fatal("resolveRepo() error = nil, want -R/--repo is required")
	}
	if err.Error() != "-R/--repo is required" {
		t.Errorf("resolveRepo() error = %q, want %q", err.Error(), "-R/--repo is required")
	}
}

func TestResolveRepo_NotRequired_NeitherSet_ReturnsEmpty(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "")

	c := newRepoTestCmd(t)

	got, err := resolveRepo(c, false)
	if err != nil {
		t.Fatalf("resolveRepo() error = %v", err)
	}
	if got != "" {
		t.Errorf("resolveRepo() = %q, want empty", got)
	}
}

func TestResolveRepoID_ParsesUUID(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "")

	c := newRepoTestCmd(t)
	if err := c.Flags().Set("repo", testRepoID); err != nil {
		t.Fatalf("Set(--repo) error = %v", err)
	}

	id, present, err := resolveRepoID(c, true)
	if err != nil {
		t.Fatalf("resolveRepoID() error = %v", err)
	}
	if !present {
		t.Error("resolveRepoID() present = false, want true")
	}
	if id.String() != testRepoID {
		t.Errorf("resolveRepoID() id = %q, want %q", id.String(), testRepoID)
	}
}

func TestResolveRepoID_InvalidUUID_Errors(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "")

	c := newRepoTestCmd(t)
	if err := c.Flags().Set("repo", "not-a-uuid"); err != nil {
		t.Fatalf("Set(--repo) error = %v", err)
	}

	_, _, err := resolveRepoID(c, true)
	if err == nil {
		t.Fatal("resolveRepoID() error = nil, want an invalid-UUID error")
	}
}

func TestResolveRepoID_NotRequired_Absent_NotPresent(t *testing.T) {
	t.Setenv("FEEDCTL_REPO", "")

	c := newRepoTestCmd(t)

	id, present, err := resolveRepoID(c, false)
	if err != nil {
		t.Fatalf("resolveRepoID() error = %v", err)
	}
	if present {
		t.Error("resolveRepoID() present = true, want false")
	}
	if id.String() != "00000000-0000-0000-0000-000000000000" {
		t.Errorf("resolveRepoID() id = %q, want the zero UUID", id.String())
	}
}

// --- -S/--source (addSourceFlag/resolveSourceID) ---

func newSourceFlagTestCmd(t *testing.T) *cobra.Command {
	t.Helper()

	c := &cobra.Command{Use: "x"}
	addSourceFlag(c)

	return c
}

func TestResolveSourceID_ParsesUUID(t *testing.T) {
	c := newSourceFlagTestCmd(t)
	if err := c.Flags().Set("source", testSourceID); err != nil {
		t.Fatalf("Set(--source) error = %v", err)
	}

	id, err := resolveSourceID(c)
	if err != nil {
		t.Fatalf("resolveSourceID() error = %v", err)
	}
	if id.String() != testSourceID {
		t.Errorf("resolveSourceID() = %q, want %q", id.String(), testSourceID)
	}
}

func TestResolveSourceID_Absent_Errors(t *testing.T) {
	c := newSourceFlagTestCmd(t)

	_, err := resolveSourceID(c)
	if err == nil {
		t.Fatal("resolveSourceID() error = nil, want -S/--source is required")
	}
	if err.Error() != "-S/--source is required" {
		t.Errorf("resolveSourceID() error = %q, want %q", err.Error(), "-S/--source is required")
	}
}

func TestResolveSourceID_InvalidUUID_Errors(t *testing.T) {
	c := newSourceFlagTestCmd(t)
	if err := c.Flags().Set("source", "not-a-uuid"); err != nil {
		t.Fatalf("Set(--source) error = %v", err)
	}

	_, err := resolveSourceID(c)
	if err == nil {
		t.Fatal("resolveSourceID() error = nil, want an invalid-UUID error")
	}
}
