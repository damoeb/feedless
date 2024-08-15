import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from '@angular/core';
import { fixUrl } from '../../../app.module';
import { ActivatedRoute, Router } from '@angular/router';
import { ServerConfigService } from '../../../services/server-config.service';
import { dateFormat } from '../../../services/session.service';
import { LicenseService } from '../../../services/license.service';
import '@justinribeiro/lite-youtube';
import { AppConfig } from '../../../feedless-config';
import { AppConfigService } from '../../../services/app-config.service';
import { LocalizedLicense } from '../../../graphql/types';

@Component({
  selector: 'app-about-rss-builder',
  templateUrl: './about-rss-builder.page.html',
  styleUrls: ['./about-rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutRssBuilderPage implements OnInit {
  @ViewChild('opmlPicker')
  opmlPickerElement!: ElementRef<HTMLInputElement>;

  protected readonly dateFormat = dateFormat;
  protected license: LocalizedLicense;
  protected product: AppConfig;

  constructor(
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly licenseService: LicenseService,
    private readonly appConfigService: AppConfigService,
    readonly serverConfig: ServerConfigService,
  ) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/feed-builder'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }

  getLicenseExpiry() {
    return new Date(
      this.serverConfig.getBuildFrom() + 1000 * 60 * 60 * 24 * 265 * 2,
    );
  }

  async ngOnInit() {
    const products = await this.appConfigService.getProductConfigs();
    const source = this.activatedRoute.snapshot.queryParams['source'];
    if (source) {
      await this.handleQuery(source);
    }
    this.product = products.find((app) => app.id === 'rss-proxy');
    this.changeRef.detectChanges();
    this.licenseService.licenseChange.subscribe((license) => {
      this.license = license;
      this.changeRef.detectChanges();
    });
  }
}
