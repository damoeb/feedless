import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { fixUrl } from '../../../app.module';
import { Router } from '@angular/router';
import { ServerConfigService } from '../../../services/server-config.service';
import { dateFormat } from '../../../services/session.service';

@Component({
    selector: 'app-about-page-change-tracker',
    templateUrl: './about-pc-tracker.page.html',
    styleUrls: ['./about-pc-tracker.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class AboutPcTrackerPage {
  @ViewChild('opmlPicker')
  opmlPickerElement!: ElementRef<HTMLInputElement>;

  protected readonly dateFormat = dateFormat;

  constructor(
    private readonly router: Router,
    readonly serverConfig: ServerConfigService,
  ) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/tracker-builder'], {
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
}
