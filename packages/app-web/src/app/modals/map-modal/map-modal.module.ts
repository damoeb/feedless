import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapModalComponent } from './map-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { MapModule } from '../../components/map/map.module';

@NgModule({
  declarations: [MapModalComponent],
  exports: [MapModalComponent],
  imports: [CommonModule, IonicModule, FormsModule, SearchbarModule, MapModule]
})
export class MapModalModule {}
