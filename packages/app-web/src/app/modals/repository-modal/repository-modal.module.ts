import { NgModule } from '@angular/core';
import { JsonPipe, KeyValuePipe } from '@angular/common';
import { RepositoryModalComponent } from './repository-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonButtons,
  IonCheckbox,
  IonCol,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonRadio,
  IonRadioGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonTextarea,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { FilterItemsAccordionComponent } from '../../components/filter-items-accordion/filter-items-accordion.component';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { FetchRateAccordionComponent } from '../../components/fetch-rate-accordion/fetch-rate-accordion.component';

@NgModule({
  declarations: [RepositoryModalComponent],
  exports: [RepositoryModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonLabel,
    IonContent,
    IonList,
    IonRow,
    IonCol,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonTextarea,
    FetchRateAccordionComponent,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonNote,
    IonCheckbox,
    IonRadioGroup,
    IonRadio,
    RemoveIfProdDirective,
    IonSelect,
    IonSelectOption,
    RouterLink,
    FilterItemsAccordionComponent,
    JsonPipe,
    KeyValuePipe,
  ],
})
export class RepositoryModalModule {}
