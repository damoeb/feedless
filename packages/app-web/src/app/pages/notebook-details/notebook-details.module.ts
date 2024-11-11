import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebookDetailsPage } from './notebook-details.page';
import { NotebookDetailsRoutingModule } from './notebook-details-routing.module';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
import {
  IonSplitPane,
  IonMenu,
  IonHeader,
  IonToolbar,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonSearchbar,
  IonList,
  IonItem,
  IonLabel,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonText,
  IonPopover,
  IonProgressBar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    NotebookDetailsRoutingModule,
    CodeEditorModule,
    ReactiveFormsModule,
    DarkModeButtonModule,
    FormsModule,
    LoginButtonModule,
    FeedlessHeaderModule,
    RemoveIfProdModule,
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonSearchbar,
    IonList,
    IonItem,
    IonLabel,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    IonText,
    IonPopover,
    IonProgressBar,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [NotebookDetailsPage],
})
export class NotebookDetailsPageModule {}
