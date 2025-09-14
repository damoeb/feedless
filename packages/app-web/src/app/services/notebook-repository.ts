import Dexie, { Table } from 'dexie';
import { Note, Notebook, NotebookAction } from './notebook.service';
import 'dexie-observable';

class NotebookRepository extends Dexie {
  notebooks!: Table<Notebook, string>;
  notes!: Table<Note, string>;
  actions!: Table<NotebookAction, string>;

  // attachments!: Table<NotebookAction, string>;

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
    this.version(3).stores({
      notebooks: ['id'].join(', '),
      notes: ['id', 'repositoryId', 'parent', 'title'].join(', '),
      actions: ['id', 'label'].join(', '),
    });
  }
}

export const notebookRepository = new NotebookRepository();
