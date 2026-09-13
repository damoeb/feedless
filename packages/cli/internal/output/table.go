package output

import (
	"fmt"
	"io"
	"strings"
)

// Room for one truncated rune plus an ellipsis.
const minColumnWidth = 2

const columnGap = 2

// TablePrinter renders an aligned, truncated table on a TTY and headerless TSV otherwise, like gh, so pipes stay parseable.
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

// WithColor is a no-op without a TTY or with NO_COLOR, so callers needn't check.
func WithColor(colorize func(string) string) FieldOption {
	return func(f *field) { f.colorize = colorize }
}

func Bold(s string) string {
	return "\x1b[1m" + s + "\x1b[0m"
}

// NewTablePrinter takes isTTY and width as parameters so tests can inject them; width <= 0 disables truncation.
func NewTablePrinter(w io.Writer, isTTY bool, width int) *TablePrinter {
	return &TablePrinter{
		w:     w,
		isTTY: isTTY,
		color: !noColor(isTTY),
		width: width,
	}
}

// AddField sanitizes every cell; colour is applied later at render time, so it isn't stripped.
func (t *TablePrinter) AddField(text string, opts ...FieldOption) {
	f := field{text: SafeCell(text)}
	for _, opt := range opts {
		opt(&f)
	}

	t.cur = append(t.cur, f)
}

func (t *TablePrinter) EndRow() {
	t.rows = append(t.rows, t.cur)
	t.cur = nil
}

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

// The last column is exempt from shrinking: nothing after it can misalign.
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

func pad(s string, width int) string {
	n := len([]rune(s))
	if n >= width {
		return s
	}

	return s + strings.Repeat(" ", width-n)
}

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
