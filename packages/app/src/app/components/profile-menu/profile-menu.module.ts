import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileMenuComponent } from './profile-menu.component';
import { IonicModule } from '@ionic/angular';
import { ProfileSettingsModule } from '../profile-settings/profile-settings.module';

@NgModule({
  declarations: [ProfileMenuComponent],
  exports: [ProfileMenuComponent],
  imports: [CommonModule, IonicModule, ProfileSettingsModule],
})
export class ProfileMenuModule {}
