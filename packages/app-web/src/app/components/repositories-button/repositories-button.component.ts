import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { IonButton } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-repositories-button',
  templateUrl: './repositories-button.component.html',
  styleUrls: ['./repositories-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, RouterLink],
  standalone: true,
})
export class RepositoriesButtonComponent {
  readonly name = input.required<string>();

  readonly link = input.required<string>();

  constructor() {}
}
