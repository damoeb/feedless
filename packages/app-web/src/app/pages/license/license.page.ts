import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { LicenseService } from '../../services/license.service';
import { GqlLicenseQuery } from '../../../generated/graphql';
import { dateFormat } from '../../services/profile.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-license-page',
  templateUrl: './license.page.html',
  styleUrls: ['./license.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LicensePage implements OnInit, OnDestroy {
  loading = true;
  license: GqlLicenseQuery['license'];
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly serverSettings: ServerSettingsService,
    private readonly licenseService: LicenseService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
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

  protected readonly dateFormat = dateFormat;

  applyLicense(licenseRaw: string) {
    return this.licenseService.updateLicense({
      licenseRaw,
    });
  }
}
