package output

import "strings"

// replacementChar is the placeholder every stripped/neutralized control
// character is replaced with (U+FFFD REPLACEMENT CHARACTER, "�") — chosen
// over a "\xNN" escape because it's a single visible rune that can't itself
// be mistaken for the start of another escape sequence, and it's what most
// terminals already render for content they can't otherwise display.
const replacementChar = '�'

// The 8-bit C1 control code points that are the single-rune equivalents of
// the 7-bit ESC-prefixed string-type introducers/terminator SafeText
// recognizes: DCS (ESC 'P'), SOS (ESC 'X'), OSC (ESC ']'), PM (ESC '^'),
// APC (ESC '_'), and ST (ESC '\\') itself. U+009B, the 8-bit form of CSI
// (ESC '['), is deliberately not among these — see SafeText's doc comment.
const (
	c1DCS = 0x90
	c1SOS = 0x98
	c1OSC = 0x9D
	c1PM  = 0x9E
	c1APC = 0x9F
	c1ST  = 0x9C
)

// SafeText neutralizes terminal control sequences in s before it's printed
// for a human: server-provided text (record/source/repository titles and
// text, lastErrorMessage, flow descriptions, …) can otherwise embed ANSI/OSC
// escape sequences that recolour or hide output, rewrite the terminal
// title, inject an OSC 8 hyperlink, write to the clipboard via OSC 52, or
// move the cursor to overwrite earlier lines.
//
// SafeText:
//   - drops every ESC-introduced sequence in full — CSI (ESC [ … final
//     byte), OSC/DCS/SOS/PM/APC "string" sequences (ESC ] / P / X / ^ / _ …,
//     or their single-rune 8-bit-C1 equivalents U+009D/U+0090/U+0098/U+009E/
//     U+009F, terminated by BEL or ST), and the shorter "Fp/Fe/nF" escape
//     sequences (ESC plus zero or more intermediates plus one final byte) —
//     as well as a bare trailing ESC with nothing recognizable after it.
//     A string-type sequence is removed together with its content only when
//     it's properly terminated (BEL, or ST — either ESC '\\' or the 8-bit
//     U+009C) before a '\n', '\r', or another ESC; any of those (or running
//     out of input) first makes it malformed, and only its introducer is
//     dropped — see skipStringSequence for why.
//   - replaces every remaining C0 control character except '\n' and '\t',
//     every C1 control (U+0080–U+009F, except the string-type introducers
//     above, which skipStringSequence itself consumes) and DEL (U+007F)
//     with the replacement character '�' ("�");
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
		case r == 0x1B: // 7-bit ESC
			i = skipEscapeSequence(runes, i)
		case r == c1DCS || r == c1SOS || r == c1OSC || r == c1PM || r == c1APC:
			// 8-bit C1 form of a string-type introducer: same rules as its
			// 7-bit ESC-prefixed form, just a one-rune introducer instead of
			// two. (The 8-bit CSI introducer, U+009B, is deliberately not
			// handled here — it stays a plain C1 control, replaced with the
			// placeholder by isStrippedControl below, matching the 7-bit CSI
			// rule which only recognizes ESC '['.)
			i = skipStringSequence(runes, i, i+1)
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
//     APC ('_'): handed off to skipStringSequence, which removes the whole
//     sequence only when properly terminated (BEL or ST) before a line
//     break or another ESC — see its own doc comment for why.
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
		return skipStringSequence(runes, i, i+2)
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

// skipStringSequence handles one OSC/DCS/SOS/PM/APC "string-type" sequence,
// whichever way it was introduced: the 7-bit ESC-prefixed form
// (introducerEnd == introducerStart+2, e.g. "ESC ]") or the single-rune
// 8-bit C1 form (introducerEnd == introducerStart+1, e.g. U+009D). It
// returns the index to resume sanitizing at.
//
// The rule (deliberately narrow, after an earlier, broader "abort" attempt
// still let a hostile page hide arbitrary multi-line text by placing a
// second ESC anywhere after the introducer): a string-type sequence is
// removed together with its content ONLY when it is properly terminated —
// by BEL (0x07) or ST (either the 7-bit "ESC '\\'" form or the 8-bit
// U+009C) — before a '\n', '\r', or another (non-ST) ESC occurs. The moment
// any of those three, or the end of input, is reached first, the sequence
// counts as malformed: only its introducer (introducerEnd-introducerStart
// runes — never the content scanned so far) is dropped, and sanitizing
// resumes at introducerEnd, so the content collected up to that point (and
// anything after it, including a later ESC) is reprocessed as ordinary
// text/control-sequences rather than being hidden.
//
// Real string-type payloads — window/tab titles, OSC 8 hyperlink targets,
// OSC 52 clipboard payloads — never legitimately contain a newline, so this
// loses nothing a well-formed sequence would have shown, and it never hides
// more than the single (malformed) line it started on.
func skipStringSequence(runes []rune, introducerStart, introducerEnd int) int {
	n := len(runes)

	for j := introducerEnd; j < n; j++ {
		switch runes[j] {
		case 0x07, c1ST: // BEL, or the 8-bit ST terminator
			return j + 1
		case 0x1B: // 7-bit ESC: either the start of an "ESC '\\'" ST, or malformed
			if j+1 < n && runes[j+1] == '\\' {
				return j + 2 // ST
			}

			return introducerEnd // malformed: drop only the introducer
		case '\n', '\r':
			return introducerEnd // malformed: drop only the introducer
		}
	}

	return introducerEnd // ran off the end unterminated: drop only the introducer
}
