import Dexie, { Table } from 'dexie';
import { Note, Notebook } from './notebook.service';


class NotebookRepository extends Dexie {
  notebooks!: Table<Notebook, string>;
  notes!: Table<Note, string>;
  openNotes!: Table<Note, string>;

  constructor() {
    super('notebooks');
    this.version(1).stores({
      notebooks: 'id',
      notes: 'id, notebookId',
      openNotes: 'id, notebookId',
    });
  }
}

export const notebookRepository = new NotebookRepository()
