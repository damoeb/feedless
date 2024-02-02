import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
} from '@angular/core';

@Component({
  selector: 'app-wait-list-page',
  templateUrl: './wait-list.page.html',
  styleUrls: ['./wait-list.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WaitListPage {
  constructor(private readonly changeRef: ChangeDetectorRef) {}
}
