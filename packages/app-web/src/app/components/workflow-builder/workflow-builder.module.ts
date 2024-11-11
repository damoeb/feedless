import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkflowBuilderComponent } from './workflow-builder.component';
import { EmbeddedMarkupModule } from '../embedded-markup/embedded-markup.module';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { BubbleModule } from '../bubble/bubble.module';
import {
  IonToolbar,
  IonLabel,
  IonText,
  IonInput,
  IonButtons,
  IonButton,
  IonIcon,
  IonRange,
  IonItem,
  IonAccordionGroup,
  IonAccordion,
  IonList,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [WorkflowBuilderComponent],
  exports: [WorkflowBuilderComponent],
  imports: [
    CommonModule,
    EmbeddedMarkupModule,
    NativeFeedModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
    BubbleModule,
    IonToolbar,
    IonLabel,
    IonText,
    IonInput,
    IonButtons,
    IonButton,
    IonIcon,
    IonRange,
    IonItem,
    IonAccordionGroup,
    IonAccordion,
    IonList,
  ],
})
export class WorkflowBuilderModule {}
