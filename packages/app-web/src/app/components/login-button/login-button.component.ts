import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { Authentication, AuthService } from '../../services/auth.service';
import { ServerConfigService } from '../../services/server-config.service';

import { IonButton, IonIcon } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { addIcons } from 'ionicons';
import { personOutline } from 'ionicons/icons';

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styleUrls: ['./login-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, RouterLink, IonIcon],
  standalone: true,
})
export class LoginButtonComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  authorization: Authentication;
  private subscriptions: Subscription[] = [];

  readonly label = input<string>();
  readonly expand = input<string>();
  readonly color = input<string>();

  constructor() {
    addIcons({ personOutline });
  }

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
