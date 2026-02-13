import { Decoration, EditorView, PluginValue, ViewPlugin, ViewUpdate } from '@codemirror/view';

export const decorateFirstLine = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations;

    constructor(view: EditorView) {
      this.decorations = this.getDecorations(view);
    }

    update(update: ViewUpdate) {
      if (update.docChanged || update.viewportChanged) {
        this.decorations = this.getDecorations(update.view);
      }
    }

    getDecorations(view: EditorView) {
      const firstLine = view.state.doc.line(1);
      return Decoration.set(
        [
          Decoration.line({
            attributes: { class: 'cm-atx-heading-2' },
          }).range(firstLine.from),
        ],
        true
      );
    }
  },
  {
    decorations: (v) => v.decorations,
  }
);
