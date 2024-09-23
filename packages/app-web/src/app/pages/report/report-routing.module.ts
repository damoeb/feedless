import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportPage } from './report.page';

const routes: Routes = [
  {
    path: '',
    component: ReportPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportRoutingModule {}
