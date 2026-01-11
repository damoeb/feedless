import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  PLATFORM_ID,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { addOutline, ellipse, removeOutline } from 'ionicons/icons';
import {
  IonButton,
  IonButtons,
  IonInput,
  IonLabel,
  IonRange,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { isPlatformBrowser, NgStyle } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

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
    IconComponent,
    IonRange,
    FormsModule,
    NgStyle,
  ],
  standalone: true,
})
export class WorkflowBuilderComponent {
  private readonly changeRef = inject(ChangeDetectorRef);

  scaleFactor = 0.7;
  minScaleFactor = 0.5;
  maxScaleFactor = 1.3;
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ removeOutline, addOutline, ellipse });
    }
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
