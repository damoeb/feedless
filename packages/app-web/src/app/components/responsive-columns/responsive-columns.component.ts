import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { addIcons } from 'ionicons';
import { chevronForwardOutline, chevronBackOutline } from 'ionicons/icons';
import { IonGrid, IonRow, IonCol, IonIcon } from '@ionic/angular/standalone';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-responsive-columns',
  templateUrl: './responsive-columns.component.html',
  styleUrls: ['./responsive-columns.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonGrid, IonRow, IonCol, NgIf, IonIcon],
  standalone: true,
})
export class ResponsiveColumnsComponent implements AfterViewInit {
  @ViewChild('scrollpane')
  scrollpaneElement: ElementRef;

  constructor(private readonly changeRef: ChangeDetectorRef) {
    addIcons({ chevronForwardOutline, chevronBackOutline });
  }

  triggerSlider(scrollpane: HTMLDivElement) {
    scrollpane.scrollLeft === 0
      ? (scrollpane.scrollLeft = scrollpane.scrollWidth)
      : (scrollpane.scrollLeft = 0);
  }

  ngAfterViewInit(): void {
    this.scrollpaneElement.nativeElement.scrollLeft =
      this.scrollpaneElement.nativeElement.scrollWidth;
    this.changeRef.detectChanges();
  }
}
