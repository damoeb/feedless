import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from '@angular/core';

@Component({
  selector: 'app-responsive-columns',
  templateUrl: './responsive-columns.component.html',
  styleUrls: ['./responsive-columns.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiveColumnsComponent implements AfterViewInit {
  @ViewChild('scrollpane')
  scrollpaneElement: ElementRef;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

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