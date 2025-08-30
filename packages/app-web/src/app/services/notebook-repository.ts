import Dexie, { Table } from 'dexie';
import { Note, Notebook } from './notebook.service';

class NotebookRepository extends Dexie {
  notebooks!: Table<Notebook, string>;
  notes!: Table<Note, string>;

  // openNotes!: Table<Note, string>;

  constructor() {
    super('notebooks');
    const notebookIds: (keyof Notebook)[] = ['id'];
    const notesIds: (keyof Note)[] = ['id', 'repositoryId', 'parent'];
    this.version(1).stores({
      notebooks: notebookIds.join(', '),
      notes: notesIds.join(', '),
      // openNotes: notesIds.join(', '),
    });
  }
}

export const notebookRepository = new NotebookRepository();
