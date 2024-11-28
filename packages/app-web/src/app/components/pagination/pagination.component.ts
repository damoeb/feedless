import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { IonToolbar, IonButtons, IonButton } from '@ionic/angular/standalone';


@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonToolbar, IonButtons, IonButton],
  standalone: true,
})
export class PaginationComponent {
  @Input({ required: true })
  currentPage: number;

  @Input()
  isLastPage: boolean;

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter<number>();

  constructor() {}
}
