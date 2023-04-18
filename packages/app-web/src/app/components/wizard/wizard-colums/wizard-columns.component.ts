import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-columns',
  templateUrl: './wizard-columns.component.html',
  styleUrls: ['./wizard-columns.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardColumnsComponent implements OnInit {
  @ViewChild('scrollpane')
  scrollpaneElement: ElementRef;

  @Input()
  handler: WizardHandler;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit(): void {
    if (this.handler) {
      this.handler.onContextChange().subscribe((change) => {
        if (change?.feed?.create?.genericFeed) {
          this.scrollpaneElement.nativeElement.scrollLeft =
            this.scrollpaneElement.nativeElement.scrollWidth;
          this.changeRef.detectChanges();
        }
      });
    }
  }
}
