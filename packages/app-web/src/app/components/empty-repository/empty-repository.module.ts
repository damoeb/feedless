import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmptyRepositoryComponent } from './empty-repository.component';

@NgModule({
  declarations: [EmptyRepositoryComponent],
  exports: [EmptyRepositoryComponent],
  imports: [CommonModule]
})
export class EmptyRepositoryModule {
}
