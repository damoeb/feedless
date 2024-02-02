import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-wait-list-page',
  templateUrl: './wait-list.page.html',
  styleUrls: ['./wait-list.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WaitListPage implements OnInit {

  constructor(
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  ngOnInit() {
  }
}
