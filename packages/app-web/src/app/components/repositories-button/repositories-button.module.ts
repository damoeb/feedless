import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoriesButtonComponent } from './repositories-button.component';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import { IonButton } from '@ionic/angular/standalone';

@NgModule({
  declarations: [RepositoriesButtonComponent],
  exports: [RepositoriesButtonComponent],
  imports: [CommonModule, BubbleModule, RouterLink, IonButton],
})
export class RepositoriesButtonModule {}
