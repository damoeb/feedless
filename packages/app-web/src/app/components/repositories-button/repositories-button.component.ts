import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-repositories-button',
  templateUrl: './repositories-button.component.html',
  styleUrls: ['./repositories-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class RepositoriesButtonComponent {
  @Input({ required: true })
  name: string;

  @Input({ required: true })
  link: string;

  constructor() {}
}
