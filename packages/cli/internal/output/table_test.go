package output

import (
	"bytes"
	"strings"
	"testing"
)

func TestTablePrinter_NonTTY_TabSeparatedNoHeaderNoTruncation(t *testing.T) {
	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, false, 10)

	// Caller convention: only add the header row when isTTY.
	tp.AddField("this-is-a-very-long-cell-value-that-would-truncate-on-a-tty")
	tp.AddField("b")
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	want := "this-is-a-very-long-cell-value-that-would-truncate-on-a-tty\tb\n"
	if got := buf.String(); got != want {
		t.Errorf("output = %q, want %q", got, want)
	}
}

func TestTablePrinter_TTY_AlignsColumnsWithHeader(t *testing.T) {
	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, true, 0)

	tp.AddField("ID")
	tp.AddField("NAME")
	tp.EndRow()

	tp.AddField("1")
	tp.AddField("alice")
	tp.EndRow()

	tp.AddField("22")
	tp.AddField("bob")
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	got := buf.String()
	want := "ID  NAME\n1   alice\n22  bob\n"
	if got != want {
		t.Errorf("output =\n%q\nwant\n%q", got, want)
	}
}

func TestTablePrinter_TTY_TruncatesToWidth(t *testing.T) {
	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, true, 10)

	tp.AddField("this-is-a-very-long-first-column-value")
	tp.AddField("short")
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	got := strings.TrimRight(buf.String(), "\n")
	if len([]rune(got)) > 10+2+len("short") {
		t.Errorf("row %q exceeds the requested width budget", got)
	}
	if !strings.Contains(got, "…") {
		t.Errorf("row %q, want it truncated with an ellipsis", got)
	}
	if !strings.HasSuffix(got, "short") {
		t.Errorf("row %q, want the last column untouched (%q)", got, "short")
	}
}

func TestTablePrinter_Color_AppliedOnTTYWithoutNoColor(t *testing.T) {
	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, true, 0)

	tp.AddField("bold-me", WithColor(Bold))
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	if got := buf.String(); !strings.Contains(got, "\x1b[1m") {
		t.Errorf("output = %q, want it to contain the bold ANSI escape", got)
	}
}

func TestTablePrinter_Color_SuppressedByNOCOLOR(t *testing.T) {
	t.Setenv("NO_COLOR", "1")

	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, true, 0)

	tp.AddField("plain-me", WithColor(Bold))
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	if got := buf.String(); strings.Contains(got, "\x1b[") {
		t.Errorf("output = %q, want no ANSI escapes with NO_COLOR set", got)
	}
}

func TestTablePrinter_Color_SuppressedWhenNotTTY(t *testing.T) {
	buf := &bytes.Buffer{}
	tp := NewTablePrinter(buf, false, 0)

	tp.AddField("plain-me", WithColor(Bold))
	tp.EndRow()

	if err := tp.Render(); err != nil {
		t.Fatalf("Render() error = %v", err)
	}

	if got := buf.String(); strings.Contains(got, "\x1b[") {
		t.Errorf("output = %q, want no ANSI escapes when not a TTY", got)
	}
}

func TestNoColor(t *testing.T) {
	tests := []struct {
		name    string
		isTTY   bool
		noColor bool
		want    bool
	}{
		{"not a TTY", false, false, true},
		{"TTY, NO_COLOR unset", true, false, false},
		{"TTY, NO_COLOR set to empty string", true, true, true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if tt.noColor {
				t.Setenv("NO_COLOR", "")
			}

			if got := noColor(tt.isTTY); got != tt.want {
				t.Errorf("noColor(%v) = %v, want %v", tt.isTTY, got, tt.want)
			}
		})
	}
}
