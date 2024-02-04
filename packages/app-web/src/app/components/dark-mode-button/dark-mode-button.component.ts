import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dark-mode-button',
  templateUrl: './dark-mode-button.component.html',
  styleUrls: ['./dark-mode-button.component.scss'],
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

  constructor(readonly profileService: ProfileService) {}

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.profileService.watchColorScheme().subscribe((isDarkMode) => {
        this.darkMode = isDarkMode;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
