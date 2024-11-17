import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UpcomingFooterComponent } from './upcoming-footer.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DarkModeButtonModule } from '../../../components/dark-mode-button/dark-mode-button.module';
import { MapModule } from '../../../components/map/map.module';
import { IonButton, IonFooter, IonIcon } from '@ionic/angular/standalone';
import { RemoveIfProdModule } from '../../../directives/remove-if-prod/remove-if-prod.module';
import { SubmitModalModule } from '../submit-modal/submit-modal.module';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    DarkModeButtonModule,
    MapModule,
    IonButton,
    IonFooter,
    IonIcon,
    RemoveIfProdModule,
    SubmitModalModule,
  ],
  declarations: [UpcomingFooterComponent],
  exports: [UpcomingFooterComponent],
})
export class UpcomingFooterModule {}
