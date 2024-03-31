import { Decoration, DecorationSet, EditorView, MatchDecorator, PluginValue, ViewPlugin, ViewUpdate, WidgetType } from '@codemirror/view';
import { NotebookService } from '../services/notebook.service';

export function createNoteReferenceWidget(notebookService: NotebookService) {
  class NoteLinkWidget extends WidgetType {
    private readonly widgetLink: HTMLElement;
    private classNames: string[] = [];
    constructor(private readonly noteId: string) {
      super();

      this.widgetLink = document.createElement('a');
      this.widgetLink.setAttribute('href', 'javascript:void(0)');
      this.classNames.push('note-link');
      this.init();
    }

    override eq(other: NoteLinkWidget) {
      return other.noteId == this.noteId;
    }

    override toDOM() {
      return this.widgetLink;
    }

    override ignoreEvent() {
      return false;
    }

    private async init() {
      const note = await notebookService.findByNamedId(this.noteId);
      if (note) {
        this.classNames.push('note-link--valid');
        this.widgetLink.textContent = note.title.trim() || 'Open Note';
        this.widgetLink.addEventListener('click', () =>
          notebookService.openNote(note),
        );
      } else {
        this.classNames.push('note-link--invalid');
        this.widgetLink.textContent = 'Create Note';
        this.widgetLink.addEventListener('click', () =>
          notebookService.createNote({ namedId: this.noteId }, true),
        );
      }
      this.widgetLink.setAttribute('class', this.classNames.join(' '));
    }
  }

  const noteWidget = new MatchDecorator({
    regexp: /(\[[^\]]{5,}\])/g,
    // boundary: /(\[[^\]]+\])/g,
    decorate: (
      add: (from: number, to: number, decoration: Decoration) => void,
      from: number,
      to: number,
      match: RegExpExecArray,
      view: EditorView,
    ) => {
      add(
        to,
        to,
        Decoration.widget({
          widget: new NoteLinkWidget(view.state.sliceDoc(from + 1, to - 1)),
          side: 1,
        }),
      );
    },
  });

  return ViewPlugin.fromClass(
    class implements PluginValue {
      decorations: DecorationSet;

      constructor(view: EditorView) {
        this.decorations = noteWidget.createDeco(view);
      }

      update(update: ViewUpdate) {
        this.decorations = noteWidget.updateDeco(update, this.decorations);
      }
    },
    {
      decorations: (instance) => instance.decorations,
    },
  );
}
