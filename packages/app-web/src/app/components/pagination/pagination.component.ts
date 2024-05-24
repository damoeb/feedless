import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent {

  @Input({required: true})
  currentPage: number

  @Input()
  isLastPage: boolean

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter<number>()

  constructor(
  ) {}

}
