import {
  Decoration,
  DecorationSet,
  EditorView,
  MatchDecorator,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
  WidgetType,
} from '@codemirror/view';
import { NotebookService } from '../../services/notebook.service';
import { firstValueFrom } from 'rxjs';
import { NoteHandle } from './notebook-details.page';

export function createNoteReferenceWidget(notebookService: NotebookService) {
  class NoteLinkWidget extends WidgetType {
    private readonly widgetLink: HTMLElement;
    private classNames: string[] = [];

    constructor(private readonly customId: string) {
      super();

      this.widgetLink = document.createElement('a');
      this.widgetLink.setAttribute('href', 'javascript:void(0)');
      this.classNames.push('note-link');
      this.init();
    }

    override eq(other: NoteLinkWidget) {
      return other.customId == this.customId;
    }

    override toDOM() {
      return this.widgetLink;
    }

    override ignoreEvent() {
      return false;
    }

    private async init() {
      const noteHandles = await firstValueFrom(notebookService.findByCustomId(this.customId));
      const noteHandle: NoteHandle = noteHandles.length > 0 ? noteHandles[0] : null;
      if (noteHandle) {
        this.classNames.push('note-link note-link--valid');
        this.widgetLink.textContent = noteHandle.body.title.trim() || 'Open';
        this.widgetLink.addEventListener('click', () => notebookService.openNote(noteHandle));
      } else {
        this.classNames.push('note-link note-link--invalid');
        this.widgetLink.textContent = 'Create';
        this.widgetLink.addEventListener('click', () =>
          notebookService.createNote({ customId: this.customId }, true)
        );
      }
      this.widgetLink.setAttribute('class', this.classNames.join(' '));
    }
  }

  const noteWidget = new MatchDecorator({
    regexp: /(\[[^\]]{5,}\])/g,
    // boundary: /(\[[^\]]+\])/g,
    decorate: async (
      add: (from: number, to: number, decoration: Decoration) => void,
      from: number,
      to: number,
      match: RegExpExecArray,
      view: EditorView
    ) => {
      const customId = view.state.sliceDoc(from + 1, to - 1);
      add(from, from + 1, Decoration.mark({ class: 'cm-op' }));
      add(from + 1, to - 1, Decoration.mark({ class: 'cm-note' }));
      add(to - 1, to, Decoration.mark({ class: 'cm-op' }));

      add(
        to,
        to,
        Decoration.widget({
          widget: new NoteLinkWidget(customId),
          side: 1,
        })
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
    }
  );
}
