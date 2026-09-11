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
// starts at runes[i] (runes[i] == ESC), consuming only the bytes that are
// actually part of a recognized sequence — never more — so a malformed or
// unterminated sequence can never hide legitimate text that follows it (a
// hostile page could otherwise use an unterminated sequence to make every
// later line of a record's text, or a harvest log, disappear). Recognizes:
//   - CSI (ESC '[' …): parameter bytes 0x30–0x3F and intermediate bytes
//     0x20–0x2F, then one final byte 0x40–0x7E. The first byte outside the
//     combined 0x20–0x7E parameter/intermediate/final range ends the
//     sequence right there — that byte, and everything after it, is left
//     for normal processing rather than being swallowed as if it were part
//     of the sequence.
//   - the "string" introducers OSC (']'), DCS ('P'), SOS ('X'), PM ('^'),
//     APC ('_'): consumed up to and including a BEL (0x07) or ST (ESC '\\')
//     terminator. An ESC that isn't the start of an ST aborts the sequence
//     right there too (xterm's own behaviour), handing that ESC back to be
//     reprocessed as a fresh, independent escape sequence. If neither BEL
//     nor ST nor an aborting ESC ever appears (the sequence runs off the
//     end of the input unterminated), only its two-rune introducer (ESC
//     plus the type character) is dropped — the rest of the would-be
//     payload is left for normal processing (and so still shown, sanitized
//     like any other text) instead of being hidden in its entirety.
//   - any other "nF"/"Fp"/"Fe"-style sequence: zero or more intermediate
//     bytes (0x20–0x2F) followed by one final byte (0x30–0x7E).
//
// An ESC not followed by anything recognizable drops just the ESC itself.
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
			if c >= 0x40 && c <= 0x7E { // final byte: properly terminated
				return j + 1
			}
			if c < 0x20 || c > 0x7E { // outside parameter/intermediate/final: malformed
				return j // don't consume the byte that broke the sequence
			}

			j++ // 0x20-0x3F: parameter/intermediate byte, keep scanning
		}

		// Ran off the end while every byte seen was still a valid
		// parameter/intermediate byte and no final byte ever appeared —
		// there's nothing left after n to preserve either way.
		return n
	case ']', 'P', 'X', '^', '_': // OSC, DCS, SOS, PM, APC
		j := i + 2
		for j < n {
			c := runes[j]
			if c == 0x07 { // BEL terminator
				return j + 1
			}
			if c == 0x1B { // ESC: either an ST terminator, or aborts the sequence
				if j+1 < n && runes[j+1] == '\\' {
					return j + 2 // ST
				}

				return j // abort: reprocess this ESC fresh, consuming nothing of it
			}

			j++
		}

		// Unterminated: drop only the two-rune introducer, not the payload
		// collected above — it's reprocessed as ordinary text/control
		// characters instead of being hidden.
		return i + 2
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
