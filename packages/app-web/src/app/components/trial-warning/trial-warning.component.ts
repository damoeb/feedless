import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlLicenseQuery } from '../../../generated/graphql';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { LicenseService } from '../../services/license.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-trial-warning',
  templateUrl: './trial-warning.component.html',
  styleUrls: ['./trial-warning.component.scss'],
})
export class TrialWarningComponent implements OnInit, OnDestroy {
  license: GqlLicenseQuery['license'];
  trialEndIn: string;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    readonly serverSettings: ServerSettingsService,
    private readonly licenseService: LicenseService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    dayjs.extend(relativeTime);
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.trialEndIn = dayjs(license.trialUntil).toNow(true);
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  isTrialPeriod() {
    return (
      this.license &&
      !this.license.isLocated &&
      this.license.trialUntil > new Date().getTime()
    );
  }

}
