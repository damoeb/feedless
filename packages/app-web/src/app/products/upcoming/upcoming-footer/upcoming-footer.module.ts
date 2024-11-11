import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UpcomingFooterComponent } from './upcoming-footer.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DarkModeButtonModule } from '../../../components/dark-mode-button/dark-mode-button.module';
import { MapModule } from '../../../components/map/map.module';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    DarkModeButtonModule,
    MapModule,
  ],
  declarations: [UpcomingFooterComponent],
  exports: [UpcomingFooterComponent],
})
export class UpcomingFooterModule {}
