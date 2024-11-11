import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { removeOutline, addOutline, ellipse } from 'ionicons/icons';

@Component({
  selector: 'app-workflow-builder',
  templateUrl: './workflow-builder.component.html',
  styleUrls: ['./workflow-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkflowBuilderComponent {
  scaleFactor: number = 0.7;
  minScaleFactor: number = 0.5;
  maxScaleFactor: number = 1.3;

  constructor(private readonly changeRef: ChangeDetectorRef) {
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
