import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ArticleEditorPage } from './article-editor.page';

const routes: Routes = [
  {
    path: '',
    component: ArticleEditorPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ArticleEditorPageRoutingModule {}
