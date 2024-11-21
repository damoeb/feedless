import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoryModalComponent } from './repository-modal.component';
import { ReactiveFormsModule } from '@angular/forms';
import { FilterItemsAccordionModule } from '../../components/filter-items-accordion/filter-items-accordion.module';
import { FetchRateAccordionModule } from '../../components/fetch-rate-accordion/fetch-rate-accordion.module';
import { RouterLink } from '@angular/router';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
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

@NgModule({
  declarations: [RepositoryModalComponent],
  exports: [RepositoryModalComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FilterItemsAccordionModule,
    FetchRateAccordionModule,
    RouterLink,
    RemoveIfProdModule,
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
    IonTextarea,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonNote,
    IonCheckbox,
    IonRadioGroup,
    IonRadio,
    IonSelect,
    IonSelectOption,
  ],
})
export class RepositoryModalModule {}
