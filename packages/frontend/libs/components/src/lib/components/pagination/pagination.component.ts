import { ChangeDetectionStrategy, Component, effect, input, output } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonCol,
  IonRow,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButtons, IonButton, ReactiveFormsModule, IonRow, IonCol, IonSelect, IonSelectOption],
  standalone: true,
})
export class PaginationComponent {
  readonly currentPage = input.required<number>();

  readonly isLastPage = input<boolean>();
  readonly showPageSize = input<boolean>(false);
  readonly pageSize = input<number>(10);

  readonly pageChange = output<number>();
  readonly pageSizeChange = output<number>();

  pageSizeFc = new FormControl<number>(this.pageSize(), [
    Validators.min(1),
    Validators.max(50),
    Validators.required,
  ]);

  constructor() {
    effect(() => {
      this.pageSizeFc.setValue(this.pageSize(), { emitEvent: false });
    });

    this.pageSizeFc.valueChanges.subscribe((pageSize) => {
      if (this.pageSizeFc.valid) {
        this.pageSizeChange.emit(pageSize);
      }
    });
  }
}
