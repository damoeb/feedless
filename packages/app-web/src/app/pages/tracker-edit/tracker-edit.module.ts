import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackerEditPage } from './tracker-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TrackerEditRoutingModule } from './tracker-edit-routing.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { InteractiveWebsiteModule } from '../../components/interactive-website/interactive-website.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonReorder,
  IonReorderGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TrackerEditRoutingModule,
    SearchbarModule,
    InteractiveWebsiteModule,
    FeedlessHeaderModule,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    IonList,
    IonReorderGroup,
    IonSelect,
    IonSelectOption,
    IonButton,
    IonIcon,
    IonReorder,
    IonRadioGroup,
    IonRadio,
  ],
  declarations: [TrackerEditPage],
})
export class TrackerEditPageModule {}
