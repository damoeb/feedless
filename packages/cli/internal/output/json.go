package output

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/itchyny/gojq"
	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
)

// Row maps --json field names to values.
type Row map[string]any

// AddJSONFlags registers --json and --jq; a bare --json is caught by JSONFlagErrorFunc.
func AddJSONFlags(cmd *cobra.Command, fields []string) {
	flags := cmd.Flags()

	flags.Var(&jsonFieldsValue{}, "json",
		"Output JSON, optionally restricted to a comma-separated list of fields (--json a,b or --json=a,b)")
	flags.Lookup("json").Annotations = map[string][]string{jsonFieldsAnnotation: fields}

	flags.String("jq", "", "Filter --json output with a jq expression (requires --json)")
}

const jsonFieldsAnnotation = "output.json.fields"

// jsonFieldsValue rejects values starting with "-", so "--json --jq x" isn't read as a field list.
type jsonFieldsValue struct {
	fields []string
}

func (v *jsonFieldsValue) String() string { return strings.Join(v.fields, ",") }

func (v *jsonFieldsValue) Type() string { return "jsonFields" }

func (v *jsonFieldsValue) Set(raw string) error {
	if strings.HasPrefix(raw, "-") {
		return errJSONValueLooksLikeFlag
	}

	fields := strings.Split(raw, ",")
	for i, f := range fields {
		fields[i] = strings.TrimSpace(f)
	}

	v.fields = fields

	return nil
}

var errJSONValueLooksLikeFlag = errors.New("looks like another flag, not a --json field list")

// JSONFlagErrorFunc turns a bare --json into a *FieldsError listing the command's fields, like gh.
func JSONFlagErrorFunc(_ *cobra.Command, err error) error {
	var valueRequired *pflag.ValueRequiredError
	if errors.As(err, &valueRequired) && valueRequired.GetSpecifiedName() == "json" {
		return &FieldsError{Fields: valueRequired.GetFlag().Annotations[jsonFieldsAnnotation]}
	}

	var invalidValue *pflag.InvalidValueError
	if errors.As(err, &invalidValue) && errors.Is(err, errJSONValueLooksLikeFlag) && invalidValue.GetFlag().Name == "json" {
		return &FieldsError{Fields: invalidValue.GetFlag().Annotations[jsonFieldsAnnotation]}
	}

	return err
}

func AddLimitFlag(cmd *cobra.Command, def int) {
	cmd.Flags().Int("limit", def, "Maximum number of items to fetch")
}

func ReadLimitFlag(cmd *cobra.Command) (int, error) {
	limit, err := cmd.Flags().GetInt("limit")
	if err != nil {
		return 0, fmt.Errorf("reading --limit: %w", err)
	}

	return limit, nil
}

// Fields is never empty when Requested: a bare --json never gets this far.
type JSONFlags struct {
	Requested bool
	Fields    []string
	JQ        string
}

func ReadJSONFlags(cmd *cobra.Command) (JSONFlags, error) {
	jsonFlag := cmd.Flags().Lookup("json")
	if jsonFlag == nil {
		return JSONFlags{}, errors.New("--json is not registered on this command (call output.AddJSONFlags)")
	}

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

	val, ok := jsonFlag.Value.(*jsonFieldsValue)
	if !ok {
		return JSONFlags{}, fmt.Errorf("--json flag has unexpected type %T (want *jsonFieldsValue — was it registered by output.AddJSONFlags?)", jsonFlag.Value)
	}

	return JSONFlags{Requested: true, Fields: val.fields, JQ: jq}, nil
}

// FieldsError lists the valid --json fields, like `gh <cmd> --json` without a value.
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

func PrintJSONList(w io.Writer, rows []Row, fields []string, jq string) error {
	narrowed := make([]map[string]any, len(rows))
	for i, r := range rows {
		narrowed[i] = narrow(r, fields)
	}

	return printJSON(w, narrowed, jq)
}

func PrintJSONObject(w io.Writer, row Row, fields []string, jq string) error {
	return printJSON(w, narrow(row, fields), jq)
}

// Unknown fields are dropped silently; callers validate them first.
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

	// gojq needs plain decoded-JSON values, not Go maps or structs.
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

// RunJQ prints each result as its own value, like the jq CLI.
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
