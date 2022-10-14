import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaginatedComponent } from './paginated.component';



@NgModule({
  declarations: [PaginatedComponent],
  exports: [PaginatedComponent],
  imports: [
    CommonModule
  ]
})
export class PaginatedModule { }
