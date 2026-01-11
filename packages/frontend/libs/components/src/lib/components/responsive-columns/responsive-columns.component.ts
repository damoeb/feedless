import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  inject,
  PLATFORM_ID,
  viewChild,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { chevronBackOutline, chevronForwardOutline } from 'ionicons/icons';
import { IonCol, IonGrid, IonRow } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-responsive-columns',
  templateUrl: './responsive-columns.component.html',
  styleUrls: ['./responsive-columns.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonGrid, IonRow, IonCol, IconComponent],
  standalone: true,
})
export class ResponsiveColumnsComponent implements AfterViewInit {
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly scrollpaneElement = viewChild<ElementRef>('scrollpane');
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ chevronForwardOutline, chevronBackOutline });
    }
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
