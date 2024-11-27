import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ServerConfigService } from '../../services/server-config.service';
import { GqlServerSettingsQuery } from '../../../generated/graphql';
import { LicenseService } from '../../services/license.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { Subscription } from 'rxjs';
import { IonRouterLink } from '@ionic/angular/standalone';

@Component({
    selector: 'app-trial-warning',
    templateUrl: './trial-warning.component.html',
    styleUrls: ['./trial-warning.component.scss'],
    standalone: false
})
export class TrialWarningComponent implements OnInit, OnDestroy {
  license: GqlServerSettingsQuery['serverSettings']['license'];
  trialEndIn: string;
  private subscriptions: Subscription[] = [];

  constructor(
    readonly serverConfig: ServerConfigService,
    private readonly licenseService: LicenseService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

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
