package output

import (
	"bytes"
	"encoding/json"
	"strings"
	"testing"

	"github.com/spf13/cobra"
)

func newTestCmdWithJSONFlags() *cobra.Command {
	c := &cobra.Command{Use: "test", RunE: func(*cobra.Command, []string) error { return nil }}
	AddJSONFlags(c)

	return c
}

func TestReadJSONFlags_NotRequested(t *testing.T) {
	c := newTestCmdWithJSONFlags()
	c.SetArgs([]string{})

	if err := c.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(c)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	if flags.Requested {
		t.Errorf("Requested = true, want false")
	}
}

func TestReadJSONFlags_NoValue_RequestedWithNoFields(t *testing.T) {
	c := newTestCmdWithJSONFlags()
	c.SetArgs([]string{"--json"})

	if err := c.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(c)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	if !flags.Requested {
		t.Fatalf("Requested = false, want true")
	}
	if len(flags.Fields) != 0 {
		t.Errorf("Fields = %v, want empty", flags.Fields)
	}
}

// --json's value must be given with "=" (--json=a,b), not a following
// space-separated arg: NoOptDefVal (which lets bare --json list available
// fields, see TestReadJSONFlags_NoValue_RequestedWithNoFields) makes pflag
// treat a space-separated next token as a positional argument, never the
// flag's value — see pflag's parseLongArg.
func TestReadJSONFlags_WithFields(t *testing.T) {
	c := newTestCmdWithJSONFlags()
	c.SetArgs([]string{"--json=id, url ,name"})

	if err := c.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	flags, err := ReadJSONFlags(c)
	if err != nil {
		t.Fatalf("ReadJSONFlags() error = %v", err)
	}

	want := []string{"id", "url", "name"}
	if len(flags.Fields) != len(want) {
		t.Fatalf("Fields = %v, want %v", flags.Fields, want)
	}
	for i, f := range want {
		if flags.Fields[i] != f {
			t.Errorf("Fields[%d] = %q, want %q", i, flags.Fields[i], f)
		}
	}
}

func TestReadJSONFlags_JQWithoutJSON_Errors(t *testing.T) {
	c := newTestCmdWithJSONFlags()
	c.SetArgs([]string{"--jq", ".id"})

	if err := c.Execute(); err != nil {
		t.Fatalf("Execute() error = %v", err)
	}

	if _, err := ReadJSONFlags(c); err == nil {
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
