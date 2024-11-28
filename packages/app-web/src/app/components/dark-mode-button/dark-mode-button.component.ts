import { Component, Input, OnDestroy, OnInit, inject } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { sunnyOutline, moonOutline } from 'ionicons/icons';
import { IonButton, IonIcon } from '@ionic/angular/standalone';


@Component({
  selector: 'app-dark-mode-button',
  templateUrl: './dark-mode-button.component.html',
  styleUrls: ['./dark-mode-button.component.scss'],
  imports: [IonButton, IonIcon],
  standalone: true,
})
export class DarkModeButtonComponent implements OnInit, OnDestroy {
  readonly sessionService = inject(SessionService);

  darkMode: boolean;
  private subscriptions: Subscription[] = [];

  @Input()
  label: string;
  @Input()
  expand: string;
  @Input()
  color: string;

  constructor() {
    addIcons({ sunnyOutline, moonOutline });
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
