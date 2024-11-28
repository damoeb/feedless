import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { RepositoryService } from '../../services/repository.service';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import { IonContent, IonRow, IonCol } from '@ionic/angular/standalone';

@Component({
  selector: 'app-report-page',
  templateUrl: './report.page.html',
  styleUrls: ['./report.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FeedlessHeaderComponent, IonContent, IonRow, IonCol],
  standalone: true,
})
export class ReportPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    this.appConfigService.setPageTitle('Report Feed');

    this.subscriptions
      .push
      // this.activatedRoute.queryParams.subscribe(async (queryParams) => {
      //   if (queryParams.reload) {
      //     await this.fetchFeeds(0, 'network-only');
      //   }
      // }),
      ();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
