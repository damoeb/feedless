import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Session } from '../../graphql/types';
import { SessionService } from '../../services/session.service';
import { AppConfigService, ProductConfig } from '../../services/app-config.service';
import { Authentication, AuthService } from '../../services/auth.service';
import { GqlProductCategory } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';

@Component({
  selector: 'app-feedless-product-page',
  templateUrl: './feedless-product.page.html',
  styleUrls: ['./feedless-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedlessProductPage implements OnInit, OnDestroy {
  protected productConfig: ProductConfig;
  protected url: string;
  private subscriptions: Subscription[] = [];
  protected authorization: Authentication;
  protected session: Session;
  protected readonly GqlProductName = GqlProductCategory;
  protected fromNow = relativeTimeOrElse

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly authService: AuthService,
    private readonly sessionService: SessionService,
    private readonly changeRef: ChangeDetectorRef,
    readonly profile: SessionService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.sessionService.getSession().subscribe(session => {
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
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
          this.changeRef.detectChanges();
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async cancelAccountDeletion() {
    await this.sessionService.updateCurrentUser({
      purgeScheduledFor: {
        assignNull: true
      }
    });
    location.reload();
  }
}
