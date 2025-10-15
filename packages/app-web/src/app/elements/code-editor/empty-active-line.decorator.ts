import {
  Decoration,
  DecorationSet,
  EditorView,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
  WidgetType,
} from '@codemirror/view';

class EmptyLineSpanWidget extends WidgetType {
  toDOM() {
    const span = document.createElement('span');
    span.textContent = 'Type @ for options';
    span.className = 'cm-op cm-empty-line-span';
    return span;
  }

  ignoreEvent() {
    return true;
  }
}

export const decorateEmptyActiveLine = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations: DecorationSet;

    constructor(view: EditorView) {
      this.decorations = this.getDecorations(view);
    }

    update(update: ViewUpdate) {
      if (update.docChanged) {
        this.decorations = this.getDecorations(update.view);
      }
    }

    getDecorations(view: EditorView) {
      const { state } = view;
      const line = state.doc.lineAt(state.selection.main.head);
      const isEmpty = line.text.trim() === '';

      if (isEmpty) {
        return Decoration.set([
          Decoration.widget({
            widget: new EmptyLineSpanWidget(),
            side: 1,
          }).range(line.from),
        ]);
      } else {
        return Decoration.none;
      }
    }
  },
  {
    decorations: (v) => v.decorations,
  }
);
