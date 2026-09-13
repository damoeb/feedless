// Package output is the shared rendering layer, so every list and view command behaves the same.
package output

import (
	"os"

	"golang.org/x/term"
)

// Commands call IsTerminal once and pass the result on, so tests can fix it.
func IsTerminal(f *os.File) bool {
	if f == nil {
		return false
	}

	return term.IsTerminal(int(f.Fd()))
}

// TerminalWidth returns 80 when the width can't be determined.
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

// See https://no-color.org.
func noColor(isTTY bool) bool {
	if !isTTY {
		return true
	}

	_, set := os.LookupEnv("NO_COLOR")

	return set
}
