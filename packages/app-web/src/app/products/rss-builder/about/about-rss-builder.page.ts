import { ChangeDetectionStrategy, Component } from '@angular/core';
import { fixUrl } from '../../../app.module';
import { Router } from '@angular/router';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { dateFormat, dateTimeFormat } from '../../../services/profile.service';

@Component({
  selector: 'app-about-rss-builder',
  templateUrl: './about-rss-builder.page.html',
  styleUrls: ['./about-rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutRssBuilderPage {
  constructor(private readonly router: Router,
              readonly serverSettings: ServerSettingsService) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }

  getLicenseExpiry() {
    return new Date(this.serverSettings.getBuildFrom() + 1000 * 60 * 60 * 24 * 265 * 2);
  }

  protected readonly dateFormat = dateFormat;
}
