import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsButtonComponent } from './notifications-button.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [NotificationsButtonComponent],
  exports: [NotificationsButtonComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class NotificationsButtonModule {}
