package output

import "testing"

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
	got := SafeText("a\u0080b\u009fc") // U+0080 (lowest C1) and U+009F (highest C1)
	want := "a�b�c"
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
