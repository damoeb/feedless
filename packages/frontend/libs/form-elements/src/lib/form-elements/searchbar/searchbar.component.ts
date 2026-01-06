import { Component, input, OnChanges, OnInit, output, SimpleChanges } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';
import { IonButton, IonIcon, IonInput, IonSpinner } from '@ionic/angular/standalone';

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss'],
  imports: [FormsModule, IonInput, ReactiveFormsModule, IonButton, IonIcon, IonSpinner],
  standalone: true,
})
export class SearchbarComponent implements OnInit, OnChanges {
  readonly value = input<string>();

  readonly loading = input<boolean>();

  readonly buttonText = input<string>();

  readonly placeholder = input<string>();

  readonly color = input<string>();

  readonly querySubmit = output<string>();

  readonly cancelReceived = output<void>();

  queryFc = new FormControl<string>('', [Validators.required]);

  constructor() {
    addIcons({ chevronForwardOutline });
  }

  triggerUpdate() {
    this.querySubmit.emit(this.queryFc.value);
  }

  ngOnInit(): void {
    this.queryFc.setValue(this.value());
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.value) {
      this.queryFc.setValue(changes.value.currentValue);
    }
  }
}
