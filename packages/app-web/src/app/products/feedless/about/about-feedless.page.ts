import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';

// type RssBuilderUseCase = {
//   title: string
//   description: string
//   link: string
// }

@Component({
  selector: 'app-about-feedless',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AboutFeedlessPage {

  constructor(private readonly router: Router) {

  }

  // private useCases: RssBuilderUseCase[] = [
  //   {
  //     title: 'Merge Feeds',
  //     description: 'Combining multiple feeds into a unified source streamlines information aggregation and enhances content consumption efficiency. By amalgamating diverse data streams from various sources, users can access a comprehensive and centralized view of relevant content.',
  //     link: '/merge'
  //   },
  //   {
  //     title: 'Filter Feeds',
  //     description: 'Selectively display content from the feed based on specific criteria',
  //     link: '/filter'
  //   },
  //   {
  //     title: 'Feed Digests',
  //     description: 'Summarize the feeds based on criteria and ship at scheduled intervals',
  //     link: '/digest',
  //   },
  //   {
  //     title: 'Fulltext Feed',
  //     description: 'Receive the entire content of an article or entry, rather than just a summary or excerpt',
  //     link: '/fulltext'
  //   },
  //   {
  //     title: 'Privacy Feed',
  //     description: 'Inline images and unwind shortened tracking links to maximize privacy',
  //     link: '/fulltext',
  //   },
  //   {
  //     title: 'Archive Feed',
  //     description: 'Systematic storage and preservation of the feed',
  //     link: '/archive',
  //   },
  //   {
  //     title: 'Feed Triggers',
  //     description: 'Forward feed items to webhooks or email',
  //     link: '/trigger'
  //   },
  //   {
  //     title: 'API',
  //     description: 'Forward feed items to webhooks or email',
  //     link: '/api',
  //   },
  // ]

  // getFilteredUseCases() {
  //   const filter = this.filterFc.value.toLowerCase();
  //   if (filter) {
  //     return this.useCases.filter( useCase => {
  //       return useCase.title.toLowerCase().indexOf(filter) > -1 || useCase.description.toLowerCase().indexOf(filter) > -1
  //     })
  //   } else {
  //     return this.useCases;
  //   }
  // }
  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: fixUrl(url)
        }
      });
    } catch (e) {
      console.warn(e);
    }
  }
}
