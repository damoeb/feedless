import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { ProfileSettingsComponent } from './profile-settings.component';

@NgModule({
  declarations: [ProfileSettingsComponent],
  exports: [ProfileSettingsComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class ProfileSettingsModule {}
