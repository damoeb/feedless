import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsButtonComponent } from './notifications-button.component';
import { RouterLink } from '@angular/router';
import { IonButton, IonIcon } from '@ionic/angular/standalone';

@NgModule({
  declarations: [NotificationsButtonComponent],
  exports: [NotificationsButtonComponent],
  imports: [CommonModule, RouterLink, IonButton, IonIcon],
})
export class NotificationsButtonModule {}
