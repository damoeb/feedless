import {
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { LicenseService, ServerConfigService } from '@feedless/services';
import { GqlServerSettingsQuery } from '@feedless/graphql-api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { Subscription } from 'rxjs';
import { IonButton, IonText, IonToolbar } from '@ionic/angular/standalone';

import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-trial-warning',
  templateUrl: './trial-warning.component.html',
  styleUrls: ['./trial-warning.component.scss'],
  imports: [IonToolbar, RouterLink, RouterLinkActive, IonText, IonButton],
  standalone: true,
})
export class TrialWarningComponent implements OnInit, OnDestroy {
  readonly serverConfig = inject(ServerConfigService);
  private readonly licenseService = inject(LicenseService);
  private readonly changeRef = inject(ChangeDetectorRef);

  license: GqlServerSettingsQuery['serverSettings']['license'];
  trialEndIn: string;
  private subscriptions: Subscription[] = [];

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
