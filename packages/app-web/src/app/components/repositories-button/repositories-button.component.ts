import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
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
  @Input({ required: true })
  name: string;

  @Input({ required: true })
  link: string;

  constructor() {}
}
