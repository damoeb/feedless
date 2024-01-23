import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl } from '@angular/forms';


// type RssBuilderUseCase = {
//   title: string
//   description: string
//   link: string
// }

@Component({
  selector: 'app-about-rss-builder',
  templateUrl: './about-rss-builder.page.html',
  styleUrls: ['./about-rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutRssBuilderPage {
  filterFc = new FormControl<string>('');

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
  handleQuery($event: string) {

  }
}
