import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutFeedlessPage implements OnInit {
  waitList: boolean;

  constructor(private readonly router: Router) {}

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

  ngOnInit(): void {
    this.waitList = false;
  }
}
