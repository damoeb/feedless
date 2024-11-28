import { Routes } from '@angular/router';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./documents.page').then((m) => m.DocumentsPage),
    children: [
      {
        path: 'terms',
        loadComponent: () => import('./terms.page').then((m) => m.TermsPage),
      },
      {
        path: 'telegram',
        loadComponent: () =>
          import('./telegram.page').then((m) => m.TelegramPage),
      },
    ],
  },
];
