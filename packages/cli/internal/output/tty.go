// Package output is feedctl's shared rendering layer: the table printer
// every list/view command uses for its default (human) output, the
// --json/--jq machinery for its machine-readable output, and the --limit
// pagination helper for list commands. Every entity command (source,
// harvest, …) builds on this instead of formatting its own output, so
// `feedctl X list` and `feedctl Y list` behave identically.
package output

import (
	"os"

	"golang.org/x/term"
)

// IsTerminal reports whether f is a terminal feedctl should format tables,
// color, and truncation for. Commands call this once, on the real
// os.Stdout, and thread the result through (to NewTablePrinter, TTY-only
// JSON pretty-printing, …) so it can be overridden with a fixed value in
// tests instead of depending on the process's actual stdout.
func IsTerminal(f *os.File) bool {
	if f == nil {
		return false
	}

	return term.IsTerminal(int(f.Fd()))
}

// TerminalWidth returns f's terminal width in columns, or 80 if it can't be
// determined (f isn't a terminal, or the ioctl fails). Like IsTerminal,
// commands call this once on the real stdout and pass the result in;
// NewTablePrinter treats width <= 0 as "don't truncate".
func TerminalWidth(f *os.File) int {
	if f == nil {
		return 80
	}

	width, _, err := term.GetSize(int(f.Fd()))
	if err != nil || width <= 0 {
		return 80
	}

	return width
}

// noColor reports whether colour should be suppressed: NO_COLOR is set (to
// any value — https://no-color.org) or the output isn't a terminal.
func noColor(isTTY bool) bool {
	if !isTTY {
		return true
	}

	_, set := os.LookupEnv("NO_COLOR")

	return set
}
