import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { AppConfigService } from '../../services/app-config.service';
import { IonCol, IonContent, IonRow } from '@ionic/angular/standalone';

@Component({
  selector: 'app-report-page',
  templateUrl: './report.page.html',
  styleUrls: ['./report.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonRow, IonCol],
  standalone: true,
})
export class ReportPage implements OnInit, OnDestroy {
  // private readonly changeRef = inject(ChangeDetectorRef);
  // private readonly activatedRoute = inject(ActivatedRoute);
  private readonly appConfigService = inject(AppConfigService);
  // private readonly repositoryService = inject(RepositoryService);

  private subscriptions: Subscription[] = [];

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
