import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoriesButtonComponent } from './repositories-button.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [RepositoriesButtonComponent],
  exports: [RepositoriesButtonComponent],
  imports: [CommonModule, IonicModule, BubbleModule, RouterLink]
})
export class RepositoriesButtonModule {}
