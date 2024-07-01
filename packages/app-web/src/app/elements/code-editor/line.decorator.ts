import { Decoration, EditorView } from '@codemirror/view';
import { StateEffect, StateField } from '@codemirror/state';

const lineHighlightMark = Decoration.line({
  attributes: { class: 'hl-line' },
});

export const addLineHighlight = StateEffect.define<number>();

export const lineHighlightField = StateField.define({
  create() {
    return Decoration.none;
  },
  update(lines, tr) {
    lines = lines.map(tr.changes);
    for (let e of tr.effects) {
      if (e.is(addLineHighlight)) {
        lines = lines.update({ add: [lineHighlightMark.range(e.value)] });
        // } else {
        //   lines = Decoration.none;
      }
    }
    return lines;
  },
  provide: (f) => EditorView.decorations.from(f),
});
