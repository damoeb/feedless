package cmd

import (
	"bytes"
	"strings"
	"testing"
)

func TestRootCmd_Version(t *testing.T) {
	root := NewRootCmd("1.2.3")

	out := &bytes.Buffer{}
	root.SetOut(out)
	root.SetErr(out)
	root.SetArgs([]string{"--version"})

	if err := root.Execute(); err != nil {
		t.Fatalf("Execute() returned an error: %v", err)
	}

	got := out.String()
	want := "feedctl version 1.2.3\n"
	if got != want {
		t.Errorf("--version output = %q, want %q", got, want)
	}
}

func TestRootCmd_Use(t *testing.T) {
	root := NewRootCmd("dev")

	if !strings.HasPrefix(root.Use, "feedctl") {
		t.Errorf("root.Use = %q, want it to start with %q", root.Use, "feedctl")
	}
}
