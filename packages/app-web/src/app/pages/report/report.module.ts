import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportPage } from './report.page';
import 'img-comparison-slider';
import { ReportRoutingModule } from './report-routing.module';


import { ImportButtonModule } from '../../components/import-button/import-button.module';




import { IonCol, IonContent, IonRow } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    ReportRoutingModule,
    ImportButtonModule,
    IonContent,
    IonRow,
    IonCol,
    ReportPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ReportPageModule {}
