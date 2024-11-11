import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
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
  personOutline,
  settingsOutline,
  cardOutline,
  exitOutline,
} from 'ionicons/icons';

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styleUrls: ['./login-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginButtonComponent implements OnInit, OnDestroy {
  authorization: Authentication;
  private subscriptions: Subscription[] = [];

  @Input()
  label: string;
  @Input()
  expand: string;
  @Input()
  color: string;

  constructor(
    private readonly authService: AuthService,
    private readonly sessionService: SessionService,
    protected readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
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
