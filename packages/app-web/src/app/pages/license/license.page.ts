import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { LicenseService } from '../../services/license.service';
import { dateFormat } from '../../services/session.service';
import { Subscription } from 'rxjs';
import dayjs from 'dayjs';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { environment } from '../../../environments/environment';
import { FormControl, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { LocalizedLicense } from '../../graphql/types';
import { AppConfigService } from '../../services/app-config.service';
import { addIcons } from 'ionicons';
import {
  alertOutline,
  checkmarkDoneOutline,
  timeOutline,
} from 'ionicons/icons';

@Component({
  selector: 'app-license-page',
  templateUrl: './license.page.html',
  styleUrls: ['./license.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class LicensePage implements OnInit, OnDestroy {
  loading = true;
  license: LocalizedLicense;
  fromNow = relativeTimeOrElse;
  buyRssProxyUrl: string = `${
    environment.officialFeedlessUrl
  }/pricing/rss-proxy?callbackUrl=${encodeURIComponent(location.href)}`;
  licenseFc = new FormControl<string>('', [Validators.minLength(10)]);
  protected readonly dateFormat = dateFormat;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly licenseService: LicenseService,
    private readonly appConfig: AppConfigService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ timeOutline, checkmarkDoneOutline, alertOutline });
  }

  async ngOnInit() {
    this.appConfig.setPageTitle('License');
    this.subscriptions.push(
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.licenseKey) {
          this.licenseFc.setValue(queryParams.licenseKey);
          this.changeRef.detectChanges();
        }
      }),
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.loading = false;
        this.changeRef.detectChanges();
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  applyLicense() {
    const licenseRaw = this.licenseFc.value;
    if (this.licenseFc.valid) {
      return this.licenseService.updateLicense({
        licenseRaw,
      });
    }
  }

  getRelativeTrialDaysLeft(): number {
    return (
      dayjs(this.license.trialUntil).diff(new Date().getTime(), 'days') /
      (28 * 2.0)
    );
  }
}
