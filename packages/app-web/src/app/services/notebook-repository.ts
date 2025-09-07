import Dexie, { Table } from 'dexie';
import { Note, Notebook } from './notebook.service';
import 'dexie-observable';

class NotebookRepository extends Dexie {
  notebooks!: Table<Notebook, string>;
  notes!: Table<Note, string>;

  // openNotes!: Table<Note, string>;

  constructor() {
    super('notebooks');
    this.version(1).stores({
      notebooks: ['id'].join(', '),
      notes: ['id', 'repositoryId', 'parent'].join(', '),
    });
    this.version(2).stores({
      notebooks: ['id'].join(', '),
      notes: ['id', 'repositoryId', 'parent', 'title'].join(', '),
    });
  }
}

export const notebookRepository = new NotebookRepository();
