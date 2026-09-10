package output

import (
	"bytes"
	"encoding/json"
	"reflect"
	"strings"
	"testing"

	"github.com/spf13/cobra"
)

// testFields is the field list every newTestCmdWithJSONFlags command
// registers, for tests to compare a *FieldsError's Fields against.
var testFields = []string{"id", "url", "name"}

// newTestCmdWithJSONFlags builds a small root+subcommand tree mirroring
// production: JSONFlagErrorFunc is registered on the root (as
// cmd.NewRootCmd does), and --json/--jq live on the child command (as a
// real data command would), so a bare --json is intercepted exactly the
// way it is for a real invocation like `feedctl source list --json`.
func newTestCmdWithJSONFlags() (root, sub *cobra.Command) {
	root = &cobra.Command{Use: "root", SilenceErrors: true, SilenceUsage: true}
	root.SetFlagErrorFunc(JSONFlagErrorFunc)

	sub = &cobra.Command{Use: "sub", RunE: func(*cobra.Command, []string) error { return nil }}
	AddJSONFlags(sub, testFields)
	root.AddCommand(sub)

	return root, sub
}

func TestReadJSONFlags_NotRequested(t *testing.T) {
	root, sub := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub"})

	if err := root.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(sub)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	if flags.Requested {
		t.Errorf("Requested = true, want false")
	}
}

// TestJSONFlagErrorFunc_BareJSON_AtEndOfArgs_ReturnsFieldsError covers a
// bare "--json" as the last token: pflag's own *pflag.ValueRequiredError
// (no more args to consume as the value).
func TestJSONFlagErrorFunc_BareJSON_AtEndOfArgs_ReturnsFieldsError(t *testing.T) {
	root, _ := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub", "--json"})

	err := root.Execute()
	if err == nil {
		t.Fatal("Execute() error = nil, want a *FieldsError")
	}

	fe, ok := err.(*FieldsError)
	if !ok {
		t.Fatalf("error type = %T, want *FieldsError", err)
	}
	if !reflect.DeepEqual(fe.Fields, testFields) {
		t.Errorf("Fields = %v, want %v", fe.Fields, testFields)
	}

	// "exits 1" (the brief's requirement) is main.go's default for any
	// error without its own ExitCode() — FieldsError must not override it.
	if _, hasExitCode := err.(interface{ ExitCode() int }); hasExitCode {
		t.Error("*FieldsError implements ExitCode(); it must not — it should fall back to the default exit 1")
	}
}

// TestJSONFlagErrorFunc_BareJSON_FollowedByAnotherFlag_ReturnsFieldsError
// covers "--json --jq x": without jsonFieldsValue's "-"-prefix rejection,
// pflag would silently bind "--jq" as --json's value (see jsonFieldsValue's
// doc comment) instead of recognizing this as a bare --json.
func TestJSONFlagErrorFunc_BareJSON_FollowedByAnotherFlag_ReturnsFieldsError(t *testing.T) {
	root, _ := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub", "--json", "--jq", ".id"})

	err := root.Execute()
	if err == nil {
		t.Fatal("Execute() error = nil, want a *FieldsError")
	}

	fe, ok := err.(*FieldsError)
	if !ok {
		t.Fatalf("error type = %T, want *FieldsError", err)
	}
	if !reflect.DeepEqual(fe.Fields, testFields) {
		t.Errorf("Fields = %v, want %v", fe.Fields, testFields)
	}
}

func TestReadJSONFlags_WithFields_SpaceSeparated(t *testing.T) {
	root, sub := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub", "--json", "id, url ,name"})

	if err := root.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(sub)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	want := []string{"id", "url", "name"}
	if !reflect.DeepEqual(flags.Fields, want) {
		t.Errorf("Fields = %v, want %v", flags.Fields, want)
	}
	if !flags.Requested {
		t.Error("Requested = false, want true")
	}
}

func TestReadJSONFlags_WithFields_EqualsForm(t *testing.T) {
	root, sub := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub", "--json=id, url ,name"})

	if err := root.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(sub)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	want := []string{"id", "url", "name"}
	if !reflect.DeepEqual(flags.Fields, want) {
		t.Errorf("Fields = %v, want %v", flags.Fields, want)
	}
}

func TestReadJSONFlags_JQWithoutJSON_Errors(t *testing.T) {
	root, sub := newTestCmdWithJSONFlags()
	root.SetArgs([]string{"sub", "--jq", ".id"})

	if err := root.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	if _, err := ReadJSONFlags(sub); err == nil {
		t.Fatal("ReadJSONFlags() error = nil, want an error for --jq without --json")
	}
}

func TestFieldsError_Error_ListsFields(t *testing.T) {
	err := &FieldsError{Fields: []string{"id", "url"}}

	got := err.Error()
	if !strings.Contains(got, "id") || !strings.Contains(got, "url") {
		t.Errorf("Error() = %q, want it to list both fields", got)
	}
}

func TestPrintJSONList_NoFields_PrintsEverything(t *testing.T) {
	buf := &bytes.Buffer{}
	rows := []Row{{"id": "1", "url": "http://a"}, {"id": "2", "url": "http://b"}}

	if err := PrintJSONList(buf, rows, nil, ""); err != nil {
		t.Fatalf("PrintJSONList() error = %v", err)
	}

	var got []map[string]any
	if err := json.Unmarshal(buf.Bytes(), &got); err != nil {
		t.Fatalf("output isn't valid JSON: %v (%s)", err, buf.String())
	}

	if len(got) != 2 || got[0]["id"] != "1" || got[0]["url"] != "http://a" {
		t.Errorf("got %v, want both rows with all fields", got)
	}
}

func TestPrintJSONList_WithFields_Narrows(t *testing.T) {
	buf := &bytes.Buffer{}
	rows := []Row{{"id": "1", "url": "http://a", "secret": "x"}}

	if err := PrintJSONList(buf, rows, []string{"id"}, ""); err != nil {
		t.Fatalf("PrintJSONList() error = %v", err)
	}

	var got []map[string]any
	if err := json.Unmarshal(buf.Bytes(), &got); err != nil {
		t.Fatalf("output isn't valid JSON: %v (%s)", err, buf.String())
	}

	if len(got) != 1 {
		t.Fatalf("got %d rows, want 1", len(got))
	}
	if _, ok := got[0]["url"]; ok {
		t.Errorf("got %v, want url excluded", got[0])
	}
	if got[0]["id"] != "1" {
		t.Errorf("got %v, want id=1", got[0])
	}
}

func TestPrintJSONObject_WithFields_Narrows(t *testing.T) {
	buf := &bytes.Buffer{}
	row := Row{"id": "1", "url": "http://a"}

	if err := PrintJSONObject(buf, row, []string{"id"}, ""); err != nil {
		t.Fatalf("PrintJSONObject() error = %v", err)
	}

	var got map[string]any
	if err := json.Unmarshal(buf.Bytes(), &got); err != nil {
		t.Fatalf("output isn't valid JSON: %v (%s)", err, buf.String())
	}

	if len(got) != 1 || got["id"] != "1" {
		t.Errorf("got %v, want only id=1", got)
	}
}

func TestPrintJSONList_JQFiltersOutput(t *testing.T) {
	buf := &bytes.Buffer{}
	rows := []Row{{"id": "1"}, {"id": "2"}}

	if err := PrintJSONList(buf, rows, nil, ".[].id"); err != nil {
		t.Fatalf("PrintJSONList() error = %v", err)
	}

	want := "\"1\"\n\"2\"\n"
	if got := buf.String(); got != want {
		t.Errorf("output = %q, want %q", got, want)
	}
}

func TestPrintJSONObject_JQ_InvalidExpression_Errors(t *testing.T) {
	buf := &bytes.Buffer{}

	if err := PrintJSONObject(buf, Row{"id": "1"}, nil, "not valid jq("); err == nil {
		t.Fatal("PrintJSONObject() error = nil, want an error for an invalid --jq expression")
	}
}

func TestRunJQ_SingleResult(t *testing.T) {
	buf := &bytes.Buffer{}

	if err := RunJQ(buf, ".name", map[string]any{"name": "alice"}); err != nil {
		t.Fatalf("RunJQ() error = %v", err)
	}

	want := "\"alice\"\n"
	if got := buf.String(); got != want {
		t.Errorf("output = %q, want %q", got, want)
	}
}
