package cmd

import (
	"encoding/json"
	"fmt"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// prettyFlow renders flow as indented JSON — the same shape `--flow`
// accepts back ({"sequence": [...]}) — for the editor temp file and for
// comparison against what the user saved. MarshalIndent on a ScrapeFlow
// value never fails (no channels/funcs/cyclic values in the schema); "{}"
// is an unreachable-in-practice fallback, not a real error path.
func prettyFlow(flow api.ScrapeFlow) string {
	b, err := json.MarshalIndent(flow, "", "  ")
	if err != nil {
		return "{}"
	}

	return string(b)
}

// flowActionLines renders flow's sequence as `view`'s numbered action list
// ("1. fetch https://…", "2. click //xpath", "3. extract fragment … xpath
// …" — see the brief's exact examples).
func flowActionLines(flow api.ScrapeFlow) []string {
	lines := make([]string, 0, len(flow.Sequence))
	for i, a := range flow.Sequence {
		lines = append(lines, fmt.Sprintf("%d. %s", i+1, describeAction(a)))
	}

	return lines
}

// describeAction renders one ScrapeAction as a short human-readable line.
// ScrapeAction is a "exactly one of" union (see its doc comment in
// client.gen.go); the switch picks whichever property is set.
func describeAction(a api.ScrapeAction) string {
	switch {
	case a.Fetch != nil:
		return "fetch " + describeFetch(*a.Fetch)
	case a.Click != nil:
		return "click " + describeDomElement(*a.Click)
	case a.Extract != nil:
		return describeExtract(*a.Extract)
	case a.Execute != nil:
		return "execute plugin " + a.Execute.PluginId
	case a.Header != nil:
		return fmt.Sprintf("header %s: %s", a.Header.Name, a.Header.Value)
	case a.Purge != nil:
		return "purge " + a.Purge.Value
	case a.Select != nil:
		return fmt.Sprintf("select %q at %s", a.Select.SelectValue, a.Select.Element.Value)
	case a.Type != nil:
		return fmt.Sprintf("type %q into %s", a.Type.TypeValue, a.Type.Element.Value)
	case a.WaitFor != nil:
		return "wait for " + describeDomElementByNameOrXPath(a.WaitFor.Element)
	default:
		return "(unrecognized action)"
	}
}

func describeFetch(f api.HttpFetch) string {
	if f.Get == nil {
		return "(unspecified)"
	}

	return describeStringLiteralOrVariable(f.Get.Url)
}

func describeStringLiteralOrVariable(v api.StringLiteralOrVariable) string {
	if v.Literal != nil {
		return *v.Literal
	}

	if v.Variable != nil {
		return "$" + *v.Variable
	}

	return "(unset)"
}

func describeDomElement(e api.DomElement) string {
	if e.Element != nil {
		return describeDomElementByNameOrXPath(*e.Element)
	}

	if e.Position != nil {
		return fmt.Sprintf("position (%d,%d)", e.Position.X, e.Position.Y)
	}

	return "(unspecified)"
}

func describeDomElementByNameOrXPath(e api.DomElementByNameOrXPath) string {
	if e.Xpath != nil {
		return e.Xpath.Value
	}

	if e.Name != nil {
		return "name=" + e.Name.Value
	}

	return "(unspecified)"
}

func describeExtract(e api.ScrapeExtract) string {
	if e.SelectorBased != nil {
		return fmt.Sprintf("extract fragment %s xpath %s", e.FragmentName, e.SelectorBased.Xpath.Value)
	}

	if e.ImageBased != nil {
		return fmt.Sprintf("extract fragment %s (image region)", e.FragmentName)
	}

	return "extract fragment " + e.FragmentName
}
