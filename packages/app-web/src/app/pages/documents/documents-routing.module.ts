import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';





const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./documents.page').then(m => m.DocumentsPage),
    children: [
      {
        path: 'terms',
        loadComponent: () => import('./terms.page').then(m => m.TermsPage),
      },
      {
        path: 'telegram',
        loadComponent: () => import('./telegram.page').then(m => m.TelegramPage),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DocumentsPageRoutingModule {}
