import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output
} from '@angular/core';
import {
  FormControl,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';
import {
  IonInput,
  IonButton,
  IonIcon,
  IonSpinner,
} from '@ionic/angular/standalone';


@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss'],
  imports: [
    FormsModule,
    IonInput,
    ReactiveFormsModule,
    IonButton,
    IonIcon,
    IonSpinner
],
  standalone: true,
})
export class SearchbarComponent implements OnInit, OnChanges {
  @Input()
  value: string;

  @Input()
  loading: boolean;

  @Input()
  buttonText: string;

  @Input()
  placeholder: string;

  @Input()
  color: string;

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
    this.queryFc.setValue(this.value);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.value) {
      this.queryFc.setValue(changes.value.currentValue);
    }
  }
}
