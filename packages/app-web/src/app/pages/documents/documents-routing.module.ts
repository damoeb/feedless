import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DocumentsPage } from './documents.page';
import { TermsPage } from './terms.page';
import { TelegramPage } from './telegram.page';

const routes: Routes = [
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

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DocumentsPageRoutingModule {}
