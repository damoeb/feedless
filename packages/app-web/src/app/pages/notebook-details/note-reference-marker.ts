import {
  Decoration,
  DecorationSet,
  EditorView,
  MatchDecorator,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
} from '@codemirror/view';
import { NotebookService } from '../../services/notebook.service';

export function createNoteReferenceMarker(notebookService: NotebookService) {
  const noteDecorator = new MatchDecorator({
    regexp: /(\[[^\]]{5,}\])/g,
    // boundary: /(\[[^\]]+\])/g,
    decorate: async (
      add: (from: number, to: number, decoration: Decoration) => void,
      from: number,
      to: number,
      match: RegExpExecArray,
      view: EditorView,
    ) => {
      const note = notebookService.existsById(
        view.state.sliceDoc(from + 1, to - 1),
      );
      if (note) {
        add(
          from + 1,
          to - 1,
          Decoration.mark({
            class: 'cm-note',
          }),
        );
      } else {
        add(
          from + 1,
          to - 1,
          Decoration.mark({
            class: 'cm-note cm-note--invalid',
          }),
        );
      }
    },
  });

  return ViewPlugin.fromClass(
    class implements PluginValue {
      decorations: DecorationSet;

      constructor(view: EditorView) {
        this.decorations = noteDecorator.createDeco(view);
      }

      update(update: ViewUpdate) {
        this.decorations = noteDecorator.updateDeco(update, this.decorations);
      }
    },
    {
      decorations: (instance) => instance.decorations,
    },
  );
}
