import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  OnInit,
  Output,
} from '@angular/core';

@Component({
  selector: 'app-confirm-button',
  templateUrl: './confirm-button.component.html',
  styleUrls: ['./confirm-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmButtonComponent implements OnInit {
  @Output()
  confirm = new EventEmitter();
  hasAsked: boolean;
  canConfirm: boolean;

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) {}

  ngOnInit() {
    this.hasAsked = false;
  }

  ask() {
    this.hasAsked = true;
    this.canConfirm = false;
    this.changeDetectorRef.detectChanges();
    setTimeout(() => {
      this.canConfirm = true;
      this.changeDetectorRef.detectChanges();
    }, 400);
    setTimeout(() => {
      this.hasAsked = false;
      this.changeDetectorRef.detectChanges();
    }, 3000);
  }
}
