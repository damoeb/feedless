import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReaderComponent } from './reader.component';
import { IonRow, IonCol } from '@ionic/angular/standalone';

@NgModule({
  declarations: [ReaderComponent],
  exports: [ReaderComponent],
  imports: [CommonModule, IonRow, IonCol],
})
export class ReaderModule {}
