import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TermsComponent } from './terms.component';



@NgModule({
  declarations: [TermsComponent],
  exports: [TermsComponent],
  imports: [
    CommonModule
  ]
})
export class TermsModule { }
