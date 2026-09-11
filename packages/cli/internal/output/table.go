package output

import (
	"fmt"
	"io"
	"strings"
)

// minColumnWidth is the narrowest a column is ever shrunk to while making a
// table fit the terminal width — enough room for one truncated rune plus an
// ellipsis.
const minColumnWidth = 2

// columnGap is the number of spaces rendered between two adjacent columns
// in TTY table mode.
const columnGap = 2

// TablePrinter renders rows of cells either as an aligned, coloured table
// (isTTY=true — header included only if the caller adds one; see
// NewTablePrinter) truncated to fit the terminal width, or as headerless,
// untruncated tab-separated rows (isTTY=false), matching gh's convention so
// table output stays pipeline-friendly when redirected.
//
// Usage:
//
//	tp := output.NewTablePrinter(cmd.OutOrStdout(), isTTY, width)
//	if isTTY {
//	    tp.AddField("ID", output.WithColor(output.Bold))
//	    tp.AddField("URL", output.WithColor(output.Bold))
//	    tp.EndRow()
//	}
//	for _, s := range sources {
//	    tp.AddField(s.Id)
//	    tp.AddField(s.Url)
//	    tp.EndRow()
//	}
//	return tp.Render()
type TablePrinter struct {
	w     io.Writer
	isTTY bool
	color bool
	width int
	rows  [][]field
	cur   []field
}

type field struct {
	text     string
	colorize func(string) string
}

// FieldOption customizes one AddField call.
type FieldOption func(*field)

// WithColor colorizes this field's text by passing its already-padded
// rendering through colorize. It's applied only in TTY mode with colour
// enabled (see NewTablePrinter) — a no-op otherwise, so callers can always
// pass it without checking isTTY/NO_COLOR themselves.
func WithColor(colorize func(string) string) FieldOption {
	return func(f *field) { f.colorize = colorize }
}

// Bold wraps s in the ANSI bold escape sequence. It's meant to be passed to
// WithColor, e.g. for a table header: output.WithColor(output.Bold).
func Bold(s string) string {
	return "\x1b[1m" + s + "\x1b[0m"
}

// NewTablePrinter builds a table printer writing to w. isTTY selects
// aligned/coloured/width-truncated rendering or headerless TSV — pass
// output.IsTerminal(os.Stdout) in production and a fixed bool in tests, so
// the TTY decision is always injectable rather than re-detected here. width
// is the terminal width used to truncate columns in TTY mode (ignored
// otherwise; pass output.TerminalWidth(os.Stdout) in production); 0 or
// negative disables truncation.
//
// Colour is suppressed — regardless of what callers pass to WithColor —
// when isTTY is false or the NO_COLOR environment variable is set to any
// value, checked once here at construction.
func NewTablePrinter(w io.Writer, isTTY bool, width int) *TablePrinter {
	return &TablePrinter{
		w:     w,
		isTTY: isTTY,
		color: !noColor(isTTY),
		width: width,
	}
}

// AddField adds one cell to the row currently being built. text is passed
// through SafeCell first — every table cell is sanitized this way,
// regardless of isTTY (TTY-aligned rendering and piped TSV alike), so a
// malicious title, url, or error message can never inject a control
// sequence through a table; colour (WithColor) is applied later, at
// render time, to the already-sanitized text, so it's never itself
// stripped.
func (t *TablePrinter) AddField(text string, opts ...FieldOption) {
	f := field{text: SafeCell(text)}
	for _, opt := range opts {
		opt(&f)
	}

	t.cur = append(t.cur, f)
}

// EndRow closes the row currently being built and starts a new one.
func (t *TablePrinter) EndRow() {
	t.rows = append(t.rows, t.cur)
	t.cur = nil
}

// Render writes every row added so far to w (closing a row left open by a
// last AddField without a matching EndRow) and returns the first write
// error, if any.
func (t *TablePrinter) Render() error {
	if len(t.cur) > 0 {
		t.EndRow()
	}

	if !t.isTTY {
		return t.renderTSV()
	}

	return t.renderTable()
}

func (t *TablePrinter) renderTSV() error {
	for _, row := range t.rows {
		cells := make([]string, len(row))
		for i, f := range row {
			cells[i] = f.text
		}

		if _, err := fmt.Fprintln(t.w, strings.Join(cells, "\t")); err != nil {
			return fmt.Errorf("writing table row: %w", err)
		}
	}

	return nil
}

func (t *TablePrinter) renderTable() error {
	widths := t.columnWidths()

	for _, row := range t.rows {
		var b strings.Builder

		last := len(row) - 1
		for i, f := range row {
			text := truncate(f.text, widths[i])
			if i != last {
				text = pad(text, widths[i])
			}

			if t.color && f.colorize != nil {
				text = f.colorize(text)
			}

			if i > 0 {
				b.WriteString(strings.Repeat(" ", columnGap))
			}

			b.WriteString(text)
		}

		if _, err := fmt.Fprintln(t.w, b.String()); err != nil {
			return fmt.Errorf("writing table row: %w", err)
		}
	}

	return nil
}

// columnWidths computes each column's natural (max content) width, then, if
// t.width is set, shrinks the widest columns one rune at a time until the
// row fits — the last column is exempt, since nothing follows it to
// misalign.
func (t *TablePrinter) columnWidths() []int {
	ncols := 0
	for _, row := range t.rows {
		if len(row) > ncols {
			ncols = len(row)
		}
	}

	if ncols == 0 {
		return nil
	}

	widths := make([]int, ncols)
	for _, row := range t.rows {
		for i, f := range row {
			if n := len([]rune(f.text)); n > widths[i] {
				widths[i] = n
			}
		}
	}

	if t.width <= 0 || ncols <= 1 {
		return widths
	}

	shrinkToFit(widths[:ncols-1], t.width-widths[ncols-1]-(ncols-1)*columnGap)

	return widths
}

// shrinkToFit reduces the widest of widths, one rune at a time, until their
// sum is at most budget or every column has hit minColumnWidth.
func shrinkToFit(widths []int, budget int) {
	for sum(widths) > budget {
		maxIdx := 0
		for i, w := range widths {
			if w > widths[maxIdx] {
				maxIdx = i
			}
		}

		if widths[maxIdx] <= minColumnWidth {
			return
		}

		widths[maxIdx]--
	}
}

func sum(ns []int) int {
	total := 0
	for _, n := range ns {
		total += n
	}

	return total
}

// pad right-pads s with spaces up to width (assumes len([]rune(s)) <= width).
func pad(s string, width int) string {
	n := len([]rune(s))
	if n >= width {
		return s
	}

	return s + strings.Repeat(" ", width-n)
}

// truncate shortens s to width runes, replacing the last one with an
// ellipsis when it had to cut anything off. s shorter than width is
// returned unchanged.
func truncate(s string, width int) string {
	runes := []rune(s)
	if len(runes) <= width {
		return s
	}

	if width <= 1 {
		return string(runes[:width])
	}

	return string(runes[:width-1]) + "…"
}
