import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlLicenseQuery } from '../../../generated/graphql';
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
    readonly serverSettings: ServerSettingsService,
    private readonly licenseService: LicenseService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  async ngOnInit() {
    dayjs.extend(relativeTime);
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
