import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { Authentication, AuthService } from '../../services/auth.service';
import { SessionService } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { addIcons } from 'ionicons';
import {
  appsOutline,
  cardOutline,
  exitOutline,
  personOutline,
  settingsOutline,
} from 'ionicons/icons';

import {
  IonButton,
  IonIcon,
  IonItem,
  IonList,
  IonPopover,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styleUrls: ['./login-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, RouterLink, IonIcon, IonPopover, IonList, IonItem],
  standalone: true,
})
export class LoginButtonComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly sessionService = inject(SessionService);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  authorization: Authentication;
  private subscriptions: Subscription[] = [];

  @Input()
  label: string;
  readonly expand = input<string>();
  readonly color = input<string>();

  constructor() {
    addIcons({
      appsOutline,
      personOutline,
      settingsOutline,
      cardOutline,
      exitOutline,
    });
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

  async logout() {
    await this.sessionService.logout();
    location.reload();
  }
}
