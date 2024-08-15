import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { TrackerEditPage } from './tracker-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TrackerEditRoutingModule } from './tracker-edit-routing.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { InteractiveWebsiteModule } from '../../components/interactive-website/interactive-website.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ReactiveFormsModule,
    TrackerEditRoutingModule,
    SearchbarModule,
    InteractiveWebsiteModule,
  ],
  declarations: [TrackerEditPage],
})
export class TrackerEditPageModule {}
