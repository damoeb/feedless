import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { IonButton, IonButtons, IonToolbar } from '@ionic/angular/standalone';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonToolbar, IonButtons, IonButton],
  standalone: true,
})
export class PaginationComponent {
  readonly currentPage = input.required<number>();

  readonly isLastPage = input<boolean>();

  readonly pageChange = output<number>();

  constructor() {}
}
