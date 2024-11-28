import { Routes } from '@angular/router';

export const WORKFLOW_BUILDER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./workflow-builder.page').then((m) => m.WorkflowBuilderPage),
  },
];
