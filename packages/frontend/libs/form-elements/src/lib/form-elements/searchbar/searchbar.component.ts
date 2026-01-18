import {
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  PLATFORM_ID,
  SimpleChanges,
} from '@angular/core';
import {
  FormControl,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';
import { IonButton, IonInput, IonSpinner } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { IconComponent } from '@feedless/components';

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss'],
  imports: [
    FormsModule,
    IonInput,
    ReactiveFormsModule,
    IonButton,
    IconComponent,
    IonSpinner,
  ],
  standalone: true,
})
export class SearchbarComponent implements OnInit, OnChanges {
  private readonly platformId = inject(PLATFORM_ID);
  readonly value = input<string>();

  readonly loading = input<boolean>();

  readonly buttonText = input<string>();

  readonly placeholder = input<string>();

  readonly color = input<string>();

  readonly querySubmit = output<string>();

  readonly cancelReceived = output<void>();

  queryFc = new FormControl<string>('', [Validators.required]);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ chevronForwardOutline });
    }
  }

  triggerUpdate() {
    this.querySubmit.emit(this.queryFc.value);
  }

  ngOnInit(): void {
    this.queryFc.setValue(this.value());
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value']) {
      this.queryFc.setValue(changes['value'].currentValue);
    }
  }
}
