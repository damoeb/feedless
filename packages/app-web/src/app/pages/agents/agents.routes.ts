import { Routes } from '@angular/router';

export const AGENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./agents.page').then((m) => m.AgentsPage),
  },
];
