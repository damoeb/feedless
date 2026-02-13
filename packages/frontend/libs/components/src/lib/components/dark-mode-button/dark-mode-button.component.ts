import {
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { SessionService } from '../../services';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { moonOutline, sunnyOutline } from 'ionicons/icons';
import { IonButton } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-dark-mode-button',
  templateUrl: './dark-mode-button.component.html',
  styleUrls: ['./dark-mode-button.component.scss'],
  imports: [IonButton, IconComponent],
  standalone: true,
})
export class DarkModeButtonComponent implements OnInit, OnDestroy {
  readonly sessionService = inject(SessionService);
  private readonly platformId = inject(PLATFORM_ID);

  darkMode: boolean;
  private subscriptions: Subscription[] = [];

  label = input<string>();
  readonly expand = input<string>();
  readonly color = input<string>();

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ sunnyOutline, moonOutline });
    }
  }

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.sessionService.watchColorScheme().subscribe((isDarkMode) => {
        this.darkMode = isDarkMode;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
