import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchbarComponent } from './searchbar.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [SearchbarComponent],
  exports: [SearchbarComponent],
  imports: [CommonModule, FormsModule, IonicModule, ReactiveFormsModule]
})
export class SearchbarModule {}
