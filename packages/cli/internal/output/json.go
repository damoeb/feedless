package output

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/itchyny/gojq"
	"github.com/spf13/cobra"
)

// Row is one item's field values, keyed by the name --json accepts for it
// (e.g. Row{"id": s.Id, "url": s.Url}). Commands build one Row per item
// (list commands) or a single Row (view commands) to hand to
// PrintJSONList/PrintJSONObject; the same keys double as the table header
// text a command chooses for TTY mode.
type Row map[string]any

// AddJSONFlags registers --json and --jq on cmd. Call it on every command
// that renders through this package (list and view alike). It doesn't
// register --limit — see AddLimitFlag for list commands.
func AddJSONFlags(cmd *cobra.Command) {
	flags := cmd.Flags()

	flags.String("json", "",
		"Output JSON; use --json=<fields> (comma-separated, needs the \"=\") to restrict it to those fields")
	// Letting --json appear with no value (list-available-fields mode, see
	// ReadJSONFlags) requires a non-empty NoOptDefVal — pflag only treats a
	// flag's argument as optional when NoOptDefVal != "", so an empty
	// string wouldn't do; jsonNoValueSentinel is never a real field list.
	flags.Lookup("json").NoOptDefVal = jsonNoValueSentinel

	flags.String("jq", "", "Filter --json output with a jq expression (requires --json)")
}

// AddLimitFlag registers --limit on cmd, for list commands only (see
// Paginate). def is the default limit — the brief specifies 30 for every
// list command C4/C5 add.
func AddLimitFlag(cmd *cobra.Command, def int) {
	cmd.Flags().Int("limit", def, "Maximum number of items to fetch")
}

// ReadLimitFlag reads back the --limit value AddLimitFlag registered.
func ReadLimitFlag(cmd *cobra.Command) (int, error) {
	limit, err := cmd.Flags().GetInt("limit")
	if err != nil {
		return 0, fmt.Errorf("reading --limit: %w", err)
	}

	return limit, nil
}

// JSONFlags is --json/--jq's parsed state for one command invocation.
// Requested is false when --json wasn't passed at all — render the normal
// table. Requested is true with Fields empty when --json was passed with no
// value: the command must build a FieldsError with its available field
// names and return it (ReadJSONFlags cannot do this itself — it doesn't
// know the command's fields). Requested is true with Fields non-empty for
// `--json a,b,c`.
type JSONFlags struct {
	Requested bool
	Fields    []string
	JQ        string
}

// ReadJSONFlags reads --json/--jq off cmd (call it in RunE, after cobra has
// parsed flags). It fails if --jq is given without --json.
func ReadJSONFlags(cmd *cobra.Command) (JSONFlags, error) {
	jsonFlag := cmd.Flags().Lookup("json")

	jq, err := cmd.Flags().GetString("jq")
	if err != nil {
		return JSONFlags{}, fmt.Errorf("reading --jq: %w", err)
	}

	if !jsonFlag.Changed {
		if jq != "" {
			return JSONFlags{}, errors.New("--jq requires --json")
		}

		return JSONFlags{}, nil
	}

	out := JSONFlags{Requested: true, JQ: jq}

	if value := jsonFlag.Value.String(); value != "" && value != jsonNoValueSentinel {
		for _, f := range strings.Split(value, ",") {
			out.Fields = append(out.Fields, strings.TrimSpace(f))
		}
	}

	return out, nil
}

// jsonNoValueSentinel is --json's NoOptDefVal: what its Value holds when
// given with no argument at all ("--json" alone, requesting the
// list-available-fields error) as opposed to Changed=false (not given) or
// an explicit field list.
const jsonNoValueSentinel = "\x00"

// FieldsError is what a command returns when --json was given with no
// field list: main's default error rendering prints it as
// "error: specify one or more …\n  <field>\n  <field>\n" and exits 1,
// mirroring `gh <cmd> --json` with no value.
type FieldsError struct {
	Fields []string
}

func (e *FieldsError) Error() string {
	lines := make([]string, 0, len(e.Fields)+1)
	lines = append(lines, "specify one or more comma-separated fields for --json:")

	for _, f := range e.Fields {
		lines = append(lines, "  "+f)
	}

	return strings.Join(lines, "\n")
}

// PrintJSONList writes rows as a JSON array to w: every key of every row
// when fields is empty, or each row narrowed to just the named keys
// otherwise. jq, when non-empty, filters the (possibly narrowed) array
// through a github.com/itchyny/gojq expression before printing — see RunJQ.
func PrintJSONList(w io.Writer, rows []Row, fields []string, jq string) error {
	narrowed := make([]map[string]any, len(rows))
	for i, r := range rows {
		narrowed[i] = narrow(r, fields)
	}

	return printJSON(w, narrowed, jq)
}

// PrintJSONObject writes row as a single JSON object to w, narrowed and
// jq-filtered the same way as PrintJSONList.
func PrintJSONObject(w io.Writer, row Row, fields []string, jq string) error {
	return printJSON(w, narrow(row, fields), jq)
}

// narrow returns a copy of r containing only the keys in fields, or all of
// r's keys when fields is empty. A field name not present in r is silently
// dropped (a command should validate field names against its own Fields()
// list before calling this, e.g. by way of FieldsError).
func narrow(r Row, fields []string) map[string]any {
	if len(fields) == 0 {
		out := make(map[string]any, len(r))
		for k, v := range r {
			out[k] = v
		}

		return out
	}

	out := make(map[string]any, len(fields))

	for _, f := range fields {
		if v, ok := r[f]; ok {
			out[f] = v
		}
	}

	return out
}

func printJSON(w io.Writer, data any, jq string) error {
	if jq == "" {
		enc := json.NewEncoder(w)
		enc.SetIndent("", "  ")

		if err := enc.Encode(data); err != nil {
			return fmt.Errorf("encoding JSON: %w", err)
		}

		return nil
	}

	// gojq operates on plain decoded-JSON values (map[string]interface{},
	// []interface{}, float64, …), not arbitrary Go structs/maps — round-trip
	// through encoding/json to get there regardless of what Row held.
	raw, err := json.Marshal(data)
	if err != nil {
		return fmt.Errorf("encoding JSON: %w", err)
	}

	var generic any
	if err := json.Unmarshal(raw, &generic); err != nil {
		return fmt.Errorf("decoding JSON for --jq: %w", err)
	}

	return RunJQ(w, jq, generic)
}

// RunJQ evaluates expr (github.com/itchyny/gojq syntax) against input (a
// value produced by encoding/json.Unmarshal into an any — not an arbitrary
// Go struct) and writes each result gojq yields as its own indented JSON
// value to w, one per line, matching the jq/gojq CLI tools' own behavior
// for an expression like `.[] | .id` that yields more than one result.
func RunJQ(w io.Writer, expr string, input any) error {
	query, err := gojq.Parse(expr)
	if err != nil {
		return fmt.Errorf("invalid --jq expression: %w", err)
	}

	code, err := gojq.Compile(query)
	if err != nil {
		return fmt.Errorf("invalid --jq expression: %w", err)
	}

	iter := code.Run(input)

	for {
		v, ok := iter.Next()
		if !ok {
			return nil
		}

		if err, ok := v.(error); ok {
			return fmt.Errorf("--jq: %w", err)
		}

		out, err := json.MarshalIndent(v, "", "  ")
		if err != nil {
			return fmt.Errorf("encoding --jq result: %w", err)
		}

		if _, err := w.Write(append(out, '\n')); err != nil {
			return fmt.Errorf("writing --jq result: %w", err)
		}
	}
}
