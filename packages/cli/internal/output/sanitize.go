package output

import "strings"

// replacementChar is the placeholder every stripped/neutralized control
// character is replaced with (U+FFFD REPLACEMENT CHARACTER, "�") — chosen
// over a "\xNN" escape because it's a single visible rune that can't itself
// be mistaken for the start of another escape sequence, and it's what most
// terminals already render for content they can't otherwise display.
const replacementChar = '�'

// SafeText neutralizes terminal control sequences in s before it's printed
// for a human: server-provided text (record/source/repository titles and
// text, lastErrorMessage, flow descriptions, …) can otherwise embed ANSI/OSC
// escape sequences that recolour or hide output, rewrite the terminal
// title, inject an OSC 8 hyperlink, write to the clipboard via OSC 52, or
// move the cursor to overwrite earlier lines.
//
// SafeText:
//   - drops every ESC-introduced sequence in full — CSI (ESC [ … final
//     byte), OSC/DCS/SOS/PM/APC "string" sequences (ESC ] / P / X / ^ / _ …
//     terminated by BEL or ST), and the shorter "Fp/Fe/nF" escape sequences
//     (ESC plus zero or more intermediates plus one final byte) — as well as
//     a bare trailing ESC with nothing recognizable after it;
//   - replaces every remaining C0 control character except '\n' and '\t',
//     every C1 control (U+0080–U+009F), and DEL (U+007F) with the
//     replacement character '�' ("�");
//   - leaves '\n', '\t', and every other printable rune (umlauts, emoji,
//     CJK, …) untouched.
//
// Apply this before a renderer adds its own intentional styling (colour,
// the poller's status line) — sanitizing already-styled text would strip
// feedctl's own escape codes too.
func SafeText(s string) string {
	runes := []rune(s)

	var b strings.Builder
	b.Grow(len(s))

	for i := 0; i < len(runes); {
		r := runes[i]

		switch {
		case r == 0x1B: // ESC
			i = skipEscapeSequence(runes, i)
		case r == '\n' || r == '\t':
			b.WriteRune(r)
			i++
		case isStrippedControl(r):
			b.WriteRune(replacementChar)
			i++
		default:
			b.WriteRune(r)
			i++
		}
	}

	return b.String()
}

// SafeCell is SafeText for a table cell: it additionally collapses '\n' and
// '\t' to a single space each, since either would otherwise break a table's
// line-per-row/column layout (TTY-aligned or TSV alike).
func SafeCell(s string) string {
	safe := SafeText(s)

	return strings.Map(func(r rune) rune {
		if r == '\n' || r == '\t' {
			return ' '
		}

		return r
	}, safe)
}

// isStrippedControl reports whether r is a control character SafeText
// replaces with the placeholder: C0 (U+0000–U+001F, '\n'/'\t' excluded —
// callers check those first), DEL (U+007F), or a C1 control
// (U+0080–U+009F).
func isStrippedControl(r rune) bool {
	switch {
	case r <= 0x1F: // C0 (ESC and '\n'/'\t' already handled by the caller)
		return true
	case r == 0x7F: // DEL
		return true
	case r >= 0x80 && r <= 0x9F: // C1
		return true
	default:
		return false
	}
}

// skipEscapeSequence returns the index just past the escape sequence that
// starts at runes[i] (runes[i] == ESC), consuming the whole sequence so none
// of it reaches the terminal. Recognizes:
//   - CSI (ESC '[' …): parameter bytes 0x30–0x3F, intermediate bytes
//     0x20–0x2F, then one final byte 0x40–0x7E.
//   - the "string" introducers OSC (']'), DCS ('P'), SOS ('X'), PM ('^'),
//     APC ('_'): consumed up to and including a BEL (0x07) or ST (ESC '\\')
//     terminator.
//   - any other "nF"/"Fp"/"Fe"-style sequence: zero or more intermediate
//     bytes (0x20–0x2F) followed by one final byte (0x30–0x7E).
//
// An unterminated CSI/string sequence, or an ESC not followed by anything
// recognizable, consumes through the input it can — never less than the ESC
// itself — so a malformed or truncated sequence can never leak a raw ESC
// (or its still-dangerous prefix) into the output.
func skipEscapeSequence(runes []rune, i int) int {
	n := len(runes)
	if i+1 >= n {
		return i + 1 // bare trailing ESC
	}

	switch runes[i+1] {
	case '[': // CSI
		j := i + 2
		for j < n {
			c := runes[j]
			if c >= 0x40 && c <= 0x7E {
				return j + 1
			}

			j++
		}

		return n // unterminated: drop the rest
	case ']', 'P', 'X', '^', '_': // OSC, DCS, SOS, PM, APC
		j := i + 2
		for j < n {
			c := runes[j]
			if c == 0x07 { // BEL terminator
				return j + 1
			}
			if c == 0x1B && j+1 < n && runes[j+1] == '\\' { // ST terminator
				return j + 2
			}

			j++
		}

		return n // unterminated: drop the rest
	default:
		j := i + 1
		for j < n && runes[j] >= 0x20 && runes[j] <= 0x2F { // intermediates
			j++
		}

		if j < n && runes[j] >= 0x30 && runes[j] <= 0x7E { // final byte
			return j + 1
		}

		// Not a recognizable escape sequence (ESC followed by a control
		// character or an out-of-range rune) — drop just the ESC and let
		// normal processing handle whatever follows.
		return i + 1
	}
}
