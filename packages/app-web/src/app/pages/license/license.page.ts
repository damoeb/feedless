import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { LicenseService } from '../../services/license.service';
import { GqlLicenseQuery } from '../../../generated/graphql';
import { dateFormat } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { StringFeatureGroup } from '../../components/plan-column/plan-column.component';
import dayjs from 'dayjs';
import { ModalController } from '@ionic/angular';
import { BuyModalComponent } from '../../modals/buy-modal/buy-modal.component';
import { relativeTimeOrElse } from '../../components/agents/agents.component';

function bold(v: string): string {
  return `<strong>${v}</strong>`;
}

function bool(value: boolean) {
  return {
    value,
  };
}

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

  featureGroupsRP: StringFeatureGroup[] = [
    {
      groupLabel: 'Features',
      features: [
        {
          title: 'Team Member',
          valueHtml: bold('1'),
        },
        {
          title: 'Feeds',
          valueHtml: bold('Infinite'),
        },
        {
          title: 'Minute Refresh Rate',
          valueHtml: bold('15'),
        },
        {
          title: 'Posts in collection',
          valueHtml: bold('Infinite'),
        },
        {
          title: 'Filters',
          valueBool: bool(true),
        },
        {
          title: 'Bundle Feeds',
          valueBool: bool(true),
        },
        {
          title: 'Full Source Available',
          valueBool: bool(true),
        },
      ],
    },
  ];
  featureGroupsRA: StringFeatureGroup[] = [
    {
      groupLabel: 'Features',
      features: [
        {
          title: 'Team Member',
          valueHtml: bold('1'),
        },
        {
          title: 'Feeds',
          valueHtml: bold('100'),
        },
        {
          title: 'Minute Refresh Rate',
          valueHtml: bold('15'),
        },
        {
          title: 'Posts in collection',
          valueHtml: bold('200'),
        },
        {
          title: 'Filters',
          valueBool: bool(true),
        },
        {
          title: 'Bundle Feeds',
          valueBool: bool(true),
        },
        {
          title: 'Full Source Available',
          valueBool: bool(false),
        },
      ],
    },
  ];

  constructor(
    private readonly licenseService: LicenseService,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  fromNow = relativeTimeOrElse

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
    if (licenseRaw.trim().length > 0) {
      return this.licenseService.updateLicense({
        licenseRaw,
      });
    }
  }

  async openBuyModal() {
    const modal = await this.modalCtrl.create({
      component: BuyModalComponent,
    });
    await modal.present();
  }

  getRelativeTrialDaysLeft(): number {
    return (
      dayjs(this.license.trialUntil).diff(new Date().getTime(), 'days') /
      (28 * 2.0)
    );
  }
}
