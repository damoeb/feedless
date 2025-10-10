import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { relativeTimeOrElse } from '../agents/agents.component';
import { GqlVertical } from '../../../generated/graphql';
import { AppConfigService, VerticalSpecWithRoutes } from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { Authentication, AuthService } from '../../services/auth.service';
import { Session } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { SessionService } from '../../services/session.service';
import {
  IonButton,
  IonButtons,
  IonHeader,
  IonIcon,
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
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';

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
    IonIcon,
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
  readonly profile = inject(SessionService);

  protected productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  protected authorization: Authentication;
  protected session: Session;
  protected readonly GqlProductName = GqlVertical;
  protected fromNow = relativeTimeOrElse;

  constructor() {
    addIcons({
      logoSlack,
      logoGithub,
      notificationsOutline,
    });
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.session = session;
      }),
      this.authService.authorizationChange().subscribe((authorization) => {
        this.authorization = authorization;
      }),
      this.appConfigService.getActiveProductConfigChange().subscribe((productConfig) => {
        this.productConfig = productConfig;
        this.changeRef.detectChanges();
      })
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
