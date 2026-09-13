package output

import "strings"

// A single visible rune that can't start another escape sequence.
const replacementChar = '�'

// 8-bit C1 forms of DCS, SOS, OSC, PM, APC and ST. CSI (U+009B) is deliberately left out.
const (
	c1DCS = 0x90
	c1SOS = 0x98
	c1OSC = 0x9D
	c1PM  = 0x9E
	c1APC = 0x9F
	c1ST  = 0x9C
)

// SafeText strips escape sequences and control characters from server-provided text, so a hostile page can't restyle, hide or overwrite output.
// Apply it before adding feedctl's own styling, or that gets stripped too.
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
			// CSI's 8-bit form (U+009B) is not handled here, matching the 7-bit rule that only knows ESC '['.
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

// SafeCell also flattens '\n' and '\t', which would break the table layout.
func SafeCell(s string) string {
	safe := SafeText(s)

	return strings.Map(func(r rune) rune {
		if r == '\n' || r == '\t' {
			return ' '
		}

		return r
	}, safe)
}

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

// skipEscapeSequence never consumes past a recognized sequence, so a malformed one can't hide the text after it.
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

		return i + 1
	}
}

// skipStringSequence removes a sequence only when BEL/ST terminates it before a line break or ESC;
// otherwise it drops just the introducer, so a hostile page can't hide the lines that follow.
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
