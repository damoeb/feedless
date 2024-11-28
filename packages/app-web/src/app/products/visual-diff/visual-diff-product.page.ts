import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { SessionService } from '../../services/session.service';
import { ChildActivationEnd, Router, RouterLink } from '@angular/router';
import { has } from 'lodash-es';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { filter, map, Subscription } from 'rxjs';
import { GqlVertical } from '../../../generated/graphql';
import { ServerConfigService } from '../../services/server-config.service';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonMenuButton,
  IonRouterOutlet,
  IonToolbar,
} from '@ionic/angular/standalone';
import { TrialWarningComponent } from '../../components/trial-warning/trial-warning.component';
import { RepositoriesButtonComponent } from '../../components/repositories-button/repositories-button.component';
import { AgentsButtonComponent } from '../../components/agents-button/agents-button.component';
import { DarkModeButtonComponent } from '../../components/dark-mode-button/dark-mode-button.component';

import { LoginButtonComponent } from '../../components/login-button/login-button.component';

@Component({
  selector: 'app-visual-diff-product-page',
  templateUrl: './visual-diff-product.page.html',
  styleUrls: ['./visual-diff-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    TrialWarningComponent,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    RouterLink,
    RepositoriesButtonComponent,
    AgentsButtonComponent,
    DarkModeButtonComponent,
    IonButton,
    LoginButtonComponent,
    IonContent,
    IonRouterOutlet,
  ],
  standalone: true,
})
export class VisualDiffProductPage implements OnInit, OnDestroy {
  readonly profile = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly router = inject(Router);
  private readonly appConfigService = inject(AppConfigService);

  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  activePageTitle: string;

  ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.router.events
        .pipe(
          filter((e) => e instanceof ChildActivationEnd),
          map((e) => (e as ChildActivationEnd).snapshot.firstChild.data),
          filter((data) => has(data, 'title')),
        )
        .subscribe((data) => {
          this.activePageTitle = data.title;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  protected readonly GqlProductName = GqlVertical;
}
