import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ScrapeService } from '../../services/scrape.service';
import { GqlScrapeRequest } from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-rss-builder-page',
  templateUrl: './scrape-api.page.html',
  styleUrls: ['./scrape-api.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScrapeApiPage implements OnInit, OnDestroy {
  url: string;
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;

  scrapeResponse: ScrapeResponse;
  loading = false;
  scrapeRequest: GqlScrapeRequest;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly scrapeService: ScrapeService,
    private readonly router: Router,
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

}
