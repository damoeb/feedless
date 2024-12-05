import { Routes } from '@angular/router';
import { DocumentsPage } from './documents.page';
import { TermsPage } from './terms.page';
import { TelegramPage } from './telegram.page';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    component: DocumentsPage,
    children: [
      {
        path: 'terms',
        component: TermsPage,
      },
      {
        path: 'telegram',
        component: TelegramPage,
      },
    ],
  },
];
