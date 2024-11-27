import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';

@Component({
    selector: 'app-about-visual-diff',
    templateUrl: './about-visual-diff.page.html',
    styleUrls: ['./about-visual-diff.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class AboutVisualDiffPage {
  constructor(private readonly router: Router) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/tracker'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }
}
