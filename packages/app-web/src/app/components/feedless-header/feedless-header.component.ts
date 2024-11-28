import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { relativeTimeOrElse } from '../agents/agents.component';
import { GqlVertical } from '../../../generated/graphql';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { Authentication, AuthService } from '../../services/auth.service';
import { Session } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { SessionService } from '../../services/session.service';
import {
  IonHeader,
  IonToolbar,
  IonLabel,
  IonButton,
  IonButtons,
  IonMenuButton,
} from '@ionic/angular/standalone';

import { RouterLink } from '@angular/router';
import { RepositoriesButtonComponent } from '../repositories-button/repositories-button.component';
import { AgentsButtonComponent } from '../agents-button/agents-button.component';
import { DarkModeButtonComponent } from '../dark-mode-button/dark-mode-button.component';
import { LoginButtonComponent } from '../login-button/login-button.component';

@Component({
  selector: 'app-feedless-header',
  templateUrl: './feedless-header.component.html',
  styleUrls: ['./feedless-header.component.scss'],
  imports: [
    IonHeader,
    IonToolbar,
    IonLabel,
    IonButton,
    IonButtons,
    IonMenuButton,
    RouterLink,
    RepositoriesButtonComponent,
    AgentsButtonComponent,
    DarkModeButtonComponent,
    LoginButtonComponent
],
  standalone: true,
})
export class FeedlessHeaderComponent implements OnInit, OnDestroy {
  protected productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  protected authorization: Authentication;
  protected session: Session;
  protected readonly GqlProductName = GqlVertical;
  protected fromNow = relativeTimeOrElse;

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly authService: AuthService,
    readonly serverConfig: ServerConfigService,
    private readonly sessionService: SessionService,
    private readonly changeRef: ChangeDetectorRef,
    readonly profile: SessionService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.session = session;
      }),
      this.authService.authorizationChange().subscribe((authorization) => {
        this.authorization = authorization;
      }),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async cancelAccountDeletion() {
    await this.sessionService.updateCurrentUser({
      purgeScheduledFor: {
        assignNull: true,
      },
    });
    location.reload();
  }
}
