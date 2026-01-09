import {
  Decoration,
  DecorationSet,
  EditorView,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
} from '@codemirror/view';
import { Range } from '@codemirror/state';

const blockquoteDecoration = Decoration.line({
  attributes: { class: 'cm-blockquote' },
});

export const decorateBlockquote = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations;

    constructor(view: EditorView) {
      this.decorations = this.getDecorations(view);
    }

    update(update: ViewUpdate) {
      if (update.docChanged) {
        this.decorations = this.getDecorations(update.view);
      }
    }

    getDecorations(view: EditorView): DecorationSet {
      const builder: Range<Decoration>[] = [];

      for (const { from, to } of view.visibleRanges) {
        let line = view.state.doc.lineAt(from);
        while (line.from <= to) {
          if (line.text.trimStart().startsWith('>')) {
            const to = line.text.indexOf('>') + 1;
            builder.push(Decoration.mark({ class: 'cm-op' }).range(line.from, line.from + to));
            builder.push(blockquoteDecoration.range(line.from));
          }

          if (line.to >= to) {
            break;
          }
          line = view.state.doc.line(line.number + 1);
        }
      }

      return Decoration.set(builder, true);
    }
  },
  {
    decorations: (v) => v.decorations,
  }
);
