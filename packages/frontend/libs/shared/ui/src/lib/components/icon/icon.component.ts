import {
  Component,
  inject,
  input,
  PLATFORM_ID,
  ChangeDetectionStrategy,
} from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-icon',
  templateUrl: './icon.component.html',
  imports: [IonIcon],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class IconComponent {
  private readonly platformId = inject(PLATFORM_ID);
  readonly name = input.required<string>();
  readonly size = input<string>();
  readonly slot = input<string>();

  readonly isBrowser = isPlatformBrowser(this.platformId);
}
