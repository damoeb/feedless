package output

import (
	"strings"
	"testing"
)

func TestSafeText_StripsCSIColour(t *testing.T) {
	got := SafeText("\x1b[31mred\x1b[0m")
	want := "red"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsCSICursorMovement(t *testing.T) {
	// Cursor up 5, clear screen, move to (1,1).
	got := SafeText("before\x1b[5Aafter\x1b[2J\x1b[1;1Hend")
	want := "beforeafterend"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsOSCTitle_BELTerminated(t *testing.T) {
	got := SafeText("before\x1b]0;evil title\x07after")
	want := "beforeafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsOSCTitle_STTerminated(t *testing.T) {
	got := SafeText("before\x1b]0;evil title\x1b\\after")
	want := "beforeafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsOSC8Hyperlink_BELTerminated(t *testing.T) {
	got := SafeText("\x1b]8;;https://evil.example\x07click me\x1b]8;;\x07")
	want := "click me"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsOSC8Hyperlink_STTerminated(t *testing.T) {
	got := SafeText("\x1b]8;;https://evil.example\x1b\\click me\x1b]8;;\x1b\\")
	want := "click me"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsOSC52ClipboardWrite(t *testing.T) {
	got := SafeText("before\x1b]52;c;ZXZpbA==\x07after")
	want := "beforeafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_StripsBareTrailingESC(t *testing.T) {
	got := SafeText("hello\x1b")
	want := "hello"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_ShortEscapeSequenceConsumesItsFinalByte(t *testing.T) {
	// ESC directly followed by a byte in the 0x30-0x7E "final byte" range
	// (here 'c', VT100's RIS/reset) is itself a complete (if terse) escape
	// sequence per ECMA-48 — both bytes are dropped, not just the ESC.
	got := SafeText("hello\x1bcworld")
	want := "helloworld"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_UnrecognizableESCDropsOnlyESC(t *testing.T) {
	// ESC followed by a C0 control character isn't a valid intermediate
	// (0x20-0x2F) or final byte (0x30-0x7E) — only the ESC itself is
	// dropped; the control character right after it is still replaced on
	// its own.
	got := SafeText("hello\x1b\x01world")
	want := "hello�world"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_ReplacesC0Controls(t *testing.T) {
	got := SafeText("a\x07b\x08c\rd") // BEL, backspace, CR
	want := "a�b�c�d"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_ReplacesC1Controls(t *testing.T) {
	// U+0080 and U+0085 (NEL) -- deliberately not U+0090/U+0098/U+009B/
	// U+009C/U+009D/U+009E/U+009F, which SafeText treats specially as
	// string-type-sequence introducers/terminator rather than as generic C1
	// controls; those are covered by their own tests below.
	got := SafeText("a\u0080b\u0085c")
	want := "a\ufffdb\ufffdc"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_ReplacesDEL(t *testing.T) {
	got := SafeText("a\x7fb")
	want := "a�b"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_KeepsNewlineAndTab(t *testing.T) {
	got := SafeText("a\nb\tc")
	want := "a\nb\tc"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_LeavesPrintableUnicodeUntouched(t *testing.T) {
	for _, s := range []string{
		"Müsli über Käse",
		"日本語のタイトル",
		"emoji party \U0001F389\U0001F680",
	} {
		if got := SafeText(s); got != s {
			t.Errorf("SafeText(%q) = %q, want unchanged", s, got)
		}
	}
}

func TestSafeText_NoBypassThroughMixedContent(t *testing.T) {
	got := SafeText("Report\x1b[31m\x1b]0;pwned\x07\x1b[0m done\x1b")
	want := "Report done"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

// --- C9 fix round 1: unterminated sequences must not hide later text ---

func TestSafeText_UnterminatedOSC_NoNewline_DoesNotHideTrailingText(t *testing.T) {
	got := SafeText("a\x1b]0;tb rest")
	want := "a0;tb rest"
	if got != want {
		t.Errorf("SafeText() = %q, want %q (only the introducer dropped, the rest shown)", got, want)
	}
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("SafeText() = %q, want no raw ESC", got)
	}
}

func TestSafeText_UnterminatedOSC_SpanningNewlines_KeepsLaterLines(t *testing.T) {
	got := SafeText("head\x1b]0;x then lots of text\nline2\nline3")
	want := "head0;x then lots of text\nline2\nline3"
	if got != want {
		t.Errorf("SafeText() = %q, want %q (line2/line3 must not be hidden)", got, want)
	}
	if !strings.Contains(got, "line2") || !strings.Contains(got, "line3") {
		t.Errorf("SafeText() = %q, want line2 and line3 preserved", got)
	}
}

func TestSafeText_UnterminatedCSI_WithEmbeddedNewline_KeepsTextAfterNewline(t *testing.T) {
	got := SafeText("head\x1b[ 12 \nnext line foo")
	want := "head\nnext line foo"
	if got != want {
		t.Errorf("SafeText() = %q, want %q (the newline and full next line must survive)", got, want)
	}
}

// TestSafeText_MalformedOSC_SecondESCAfterMultipleLines_DoesNotHideThem is
// round 2's regression case: an earlier "abort on a non-ST ESC" fix still
// dropped the entire span between the introducer and the aborting ESC —
// so a hostile page could still hide arbitrary multi-line text by placing
// a second ESC anywhere after an unterminated OSC introducer. Per the
// (deliberately narrow) rule skipStringSequence now implements, only the
// introducer itself is ever dropped once the sequence is malformed —
// content already scanned is never discarded with it.
func TestSafeText_MalformedOSC_SecondESCAfterMultipleLines_DoesNotHideThem(t *testing.T) {
	got := SafeText("head\x1b]0;x then lots of text\nline2\nline3\x1bZtail")
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("SafeText() = %q, want no raw ESC (0x1b)", got)
	}
	if !strings.Contains(got, "line2") || !strings.Contains(got, "line3") {
		t.Errorf("SafeText() = %q, want line2 and line3 preserved", got)
	}
}

func TestSafeText_MalformedOSC_SecretLineThenSecondESC_DoesNotHideLine2(t *testing.T) {
	got := SafeText("head\x1b]0;secret line1\nline2\x1bZtail")
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("SafeText() = %q, want no raw ESC (0x1b)", got)
	}
	if !strings.Contains(got, "line2") {
		t.Errorf("SafeText() = %q, want line2 preserved", got)
	}
}

func TestSafeText_OSCBodyContainsNewlineBeforeBEL_EverythingAfterIntroducerVisible(t *testing.T) {
	// The '\n' inside the would-be OSC body makes it malformed before the
	// BEL is ever reached (real OSC payloads never legitimately contain a
	// newline), so only the two-rune introducer is dropped — the '\n', the
	// literal BEL-that-never-terminated-anything (replaced with the
	// placeholder, like any other stray C0 control), and everything around
	// them stay visible.
	got := SafeText("before\x1b]0;line1\nline2\x07after")
	if strings.ContainsRune(got, 0x1b) {
		t.Errorf("SafeText() = %q, want no raw ESC (0x1b)", got)
	}
	for _, want := range []string{"before", "line1", "line2", "after"} {
		if !strings.Contains(got, want) {
			t.Errorf("SafeText() = %q, want it to contain %q", got, want)
		}
	}
}

func TestSafeText_ProperlyTerminatedOSC8AndOSC0_OneLine_RemovedButTextKept(t *testing.T) {
	// Regression guard: a real, single-line OSC 8 hyperlink and OSC 0 title
	// write — both properly BEL-terminated before any '\n'/'\r'/ESC — must
	// still be removed in full, surrounding text kept.
	got := SafeText("see \x1b]8;;https://evil.example\x07here\x1b]8;;\x07 now\x1b]0;pwned title\x07 done")
	want := "see here now done"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeText_CSIWithFinalByte_StillProperlyTerminated(t *testing.T) {
	// Regression guard alongside the unterminated-sequence fix: a
	// well-formed, terminated CSI sequence must still be dropped in full.
	got := SafeText("before\x1b[38;5;196mafter")
	want := "beforeafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

// C1 (8-bit) introducers: U+009B is the single-byte CSI equivalent of
// ESC '[', and U+009D is the single-byte OSC equivalent of ESC ']'. SafeText
// does not special-case them as sequence introducers — being C1 controls,
// isStrippedControl already replaces the raw byte itself with the
// placeholder, which independently neutralizes it (a real terminal will
// never see the byte that would have started the 8-bit sequence). These
// tests pin that this is genuinely safe — no swallowing, no leftover raw
// C1 byte — not just untested.
func TestSafeText_C1CSIIntroducer_NeutralizedWithoutSwallowingPayload(t *testing.T) {
	c1CSI := string(rune(0x9b)) // U+009B, 8-bit CSI

	got := SafeText("before" + c1CSI + "31mafter")
	want := "before�31mafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
	if strings.ContainsRune(got, 0x9b) {
		t.Errorf("SafeText() = %q, want the raw C1 byte gone", got)
	}
}

// TestSafeText_C1OSCIntroducer_RecognizedAndStrippedWhenTerminated: unlike
// the 8-bit CSI introducer above, round 2 has SafeText recognize the 8-bit
// OSC introducer (U+009D) as a genuine string-type sequence start, with the
// same termination rules as its 7-bit "ESC ]" form (see
// skipStringSequence) — a properly BEL/ST-terminated one is removed in
// full, content included, not merely neutralized byte-by-byte.
func TestSafeText_C1OSCIntroducer_RecognizedAndStrippedWhenTerminated(t *testing.T) {
	c1OSC := string(rune(0x9d)) // U+009D, 8-bit OSC

	got := SafeText("before" + c1OSC + "0;title\x07after")
	want := "beforeafter"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
	if strings.ContainsRune(got, 0x9d) {
		t.Errorf("SafeText() = %q, want the raw C1 byte gone", got)
	}
}

// TestSafeText_C1OSCIntroducer_Malformed_DropsOnlyIntroducer pins the same
// "drop only the introducer" rule for the 8-bit form: with no BEL/ST before
// the end of input, only the one-rune U+009D introducer is dropped, and the
// payload is shown (sanitized) like ordinary text instead of hidden.
func TestSafeText_C1OSCIntroducer_Malformed_DropsOnlyIntroducer(t *testing.T) {
	c1OSC := string(rune(0x9d)) // U+009D, 8-bit OSC

	got := SafeText("before" + c1OSC + "0;title unterminated")
	want := "before0;title unterminated"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
	if strings.ContainsRune(got, 0x9d) {
		t.Errorf("SafeText() = %q, want the raw C1 byte gone", got)
	}
}

func TestSafeText_RawInvalidC1Byte_ReplacedWithPlaceholder(t *testing.T) {
	got := SafeText("a" + string(rune(0x9b)) + "b")
	want := "a�b"
	if got != want {
		t.Errorf("SafeText() = %q, want %q", got, want)
	}
}

func TestSafeCell_CollapsesNewlineAndTab(t *testing.T) {
	got := SafeCell("line1\nline2\tcol")
	want := "line1 line2 col"
	if got != want {
		t.Errorf("SafeCell() = %q, want %q", got, want)
	}
}

func TestSafeCell_AlsoStripsControlSequences(t *testing.T) {
	got := SafeCell("\x1b[2Jtitle\x1b]0;evil\x07\nmore")
	want := "title more"
	if got != want {
		t.Errorf("SafeCell() = %q, want %q", got, want)
	}
}
