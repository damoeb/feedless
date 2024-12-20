import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { LocalizedLicense, ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { ServerConfigService } from '../../services/server-config.service';
import { dateFormat } from '../../services/session.service';
import { LicenseService } from '../../services/license.service';
import { ModalService } from '../../services/modal.service';
import { TrackerEditModalComponentProps } from './tracker-edit/tracker-edit-modal.component';
import { addIcons } from 'ionicons';
import { logoGithub } from 'ionicons/icons';

@Component({
  selector: 'app-change-tracker-product-page',
  templateUrl: './change-tracker-product.page.html',
  styleUrls: ['./change-tracker-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ChangeTrackerProductPage implements OnInit, OnDestroy {
  productConfig: VerticalSpecWithRoutes;
  url: string;
  private subscriptions: Subscription[] = [];
  license: LocalizedLicense;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly licenseService: LicenseService,
    private readonly modalService: ModalService,
    readonly serverConfig: ServerConfigService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ logoGithub });
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.changeRef.detectChanges();
      }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  protected readonly dateFormat = dateFormat;

  async openCreateTrackerModal() {
    const props: TrackerEditModalComponentProps = {};
    await this.modalService.openPageTrackerEditor(props);
  }
}
