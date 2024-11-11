import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InteractiveWebsiteComponent } from './interactive-website.component';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EmbeddedImageModule } from '../embedded-image/embedded-image.module';
import { EmbeddedMarkupModule } from '../embedded-markup/embedded-markup.module';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { ConsoleButtonModule } from '../console-button/console-button.module';
import {
  IonToolbar,
  IonRow,
  IonCol,
  IonInput,
  IonSegment,
  IonSegmentButton,
  IonButtons,
  IonButton,
  IonIcon,
  IonRange,
  IonLabel,
  IonText,
  IonItem,
  IonProgressBar,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [InteractiveWebsiteComponent],
  exports: [InteractiveWebsiteComponent],
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    EmbeddedImageModule,
    EmbeddedMarkupModule,
    ReactiveFormsModule,
    CodeEditorModule,
    NativeFeedModule,
    ConsoleButtonModule,
    IonToolbar,
    IonRow,
    IonCol,
    IonInput,
    IonSegment,
    IonSegmentButton,
    IonButtons,
    IonButton,
    IonIcon,
    IonRange,
    IonLabel,
    IonText,
    IonItem,
    IonProgressBar,
  ],
})
export class InteractiveWebsiteModule {}
