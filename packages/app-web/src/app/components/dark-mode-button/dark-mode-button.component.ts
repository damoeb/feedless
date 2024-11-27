import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { sunnyOutline, moonOutline } from 'ionicons/icons';

@Component({
    selector: 'app-dark-mode-button',
    templateUrl: './dark-mode-button.component.html',
    styleUrls: ['./dark-mode-button.component.scss'],
    standalone: false
})
export class DarkModeButtonComponent implements OnInit, OnDestroy {
  darkMode: boolean;
  private subscriptions: Subscription[] = [];

  @Input()
  label: string;
  @Input()
  expand: string;
  @Input()
  color: string;

  constructor(readonly sessionService: SessionService) {
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
