import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { addOutline, ellipse, removeOutline } from 'ionicons/icons';
import {
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonButtons,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonRange,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { NgStyle } from '@angular/common';
import { BubbleComponent } from '../bubble/bubble.component';

@Component({
  selector: 'app-workflow-builder',
  templateUrl: './workflow-builder.component.html',
  styleUrls: ['./workflow-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonToolbar,
    IonLabel,
    IonText,
    IonInput,
    IonButtons,
    IonButton,
    IonIcon,
    IonRange,
    FormsModule,
    NgStyle,
    IonItem,
    IonAccordionGroup,
    IonAccordion,
    IonList,
    BubbleComponent,
  ],
  standalone: true,
})
export class WorkflowBuilderComponent {
  private readonly changeRef = inject(ChangeDetectorRef);

  scaleFactor: number = 0.7;
  minScaleFactor: number = 0.5;
  maxScaleFactor: number = 1.3;

  constructor() {
    addIcons({ removeOutline, addOutline, ellipse });
  }

  zoomOut() {
    this.scaleFactor = Math.min(this.scaleFactor + 0.05, 1.3);
  }

  zoomIn() {
    this.scaleFactor = Math.max(this.scaleFactor - 0.05, 0.5);
  }

  protected readonly parent = parent;
  protected readonly parseInt = parseInt;
}
