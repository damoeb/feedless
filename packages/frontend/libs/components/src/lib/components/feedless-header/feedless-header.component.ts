import {
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { relativeTimeOrElse } from '../agents/agents.component';
import { GqlVertical, SessionResponse } from '@feedless/graphql-api';
import {
  AppConfigService,
  Authentication,
  AuthService,
  ServerConfigService,
  SessionService,
} from '../../services';
import { Subscription } from 'rxjs';
import {
  IonButton,
  IonButtons,
  IonHeader,
  IonLabel,
  IonMenuButton,
  IonToolbar,
} from '@ionic/angular/standalone';

import { RouterLink, RouterLinkActive } from '@angular/router';
import { RepositoriesButtonComponent } from '../repositories-button/repositories-button.component';
import { DarkModeButtonComponent } from '../dark-mode-button/dark-mode-button.component';
import { ProfileButtonComponent } from '../profile-button/profile-button.component';
import { addIcons } from 'ionicons';
import { logoGithub, logoSlack, notificationsOutline } from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';
import { RemoveIfProdDirective } from '../../directives';

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
    DarkModeButtonComponent,
    ProfileButtonComponent,
    IconComponent,
    RouterLinkActive,
    RemoveIfProdDirective,
  ],
  standalone: true,
})
export class FeedlessHeaderComponent implements OnInit, OnDestroy {
  private readonly appConfigService = inject(AppConfigService);
  private readonly authService = inject(AuthService);
  readonly serverConfig = inject(ServerConfigService);
  private readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);

  private subscriptions: Subscription[] = [];
  protected authorization: Authentication;
  protected session: SessionResponse;
  protected readonly GqlProductName = GqlVertical;
  protected fromNow = relativeTimeOrElse;
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({
        logoSlack,
        logoGithub,
        notificationsOutline,
      });
    }
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.session = session;
      }),
      this.authService.authorizationChange().subscribe((authorization) => {
        this.authorization = authorization;
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
