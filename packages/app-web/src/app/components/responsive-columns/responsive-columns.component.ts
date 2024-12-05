import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  inject,
  viewChild,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { chevronBackOutline, chevronForwardOutline } from 'ionicons/icons';
import { IonCol, IonGrid, IonIcon, IonRow } from '@ionic/angular/standalone';

@Component({
  selector: 'app-responsive-columns',
  templateUrl: './responsive-columns.component.html',
  styleUrls: ['./responsive-columns.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonGrid, IonRow, IonCol, IonIcon],
  standalone: true,
})
export class ResponsiveColumnsComponent implements AfterViewInit {
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly scrollpaneElement = viewChild<ElementRef>('scrollpane');

  constructor() {
    addIcons({ chevronForwardOutline, chevronBackOutline });
  }

  triggerSlider(scrollpane: HTMLDivElement) {
    scrollpane.scrollLeft === 0
      ? (scrollpane.scrollLeft = scrollpane.scrollWidth)
      : (scrollpane.scrollLeft = 0);
  }

  ngAfterViewInit(): void {
    this.scrollpaneElement().nativeElement.scrollLeft =
      this.scrollpaneElement().nativeElement.scrollWidth;
    this.changeRef.detectChanges();
  }
}
