import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ArticleService } from '../../../services/article.service';
import { ModalController } from '@ionic/angular';
import { PlayerService } from '../../../services/player.service';
import { ProfileService } from '../../../services/profile.service';
import { GqlArticleReleaseStatus } from '../../../../generated/graphql';
import {
  articleStatusToString,
  getColorForArticleStatus,
} from '../../../components/article-ref/article-ref.component';
import { Subscription } from 'rxjs';
import {
  ArticleWithContext,
  BasicContext,
  Bucket,
} from '../../../graphql/types';

@Component({
  selector: 'app-article-page',
  templateUrl: './article.page.html',
  styleUrls: ['./article.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ArticlePage implements OnInit, OnDestroy {
  @ViewChild('narrator', { static: true })
  readerContent: ElementRef;
  locale = 'de-AT';

  progress = 0;
  currentTextTrack: string;
  subtitles: boolean;
  followCursor: boolean;

  lostCursor: boolean;
  loading: boolean;
  renderFulltext = false;

  article: ArticleWithContext;
  bucketId: string;
  useFulltext: boolean;
  bucket: Bucket;
  // nativeFeed: BasicNativeFeed;
  context: BasicContext;

  private paragraphs: any[] = [];
  private currentParagraphIndex = 0;
  private articleId: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly playerService: PlayerService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly articleService: ArticleService,
  ) {}

  ngOnInit() {
    this.useFulltext = true; // this.profileService.useFulltext();
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.articleId = params.id;
        console.log('article', this.articleId);
        this.init().finally(() => {
          this.playerService.pushFirst(this.article);
          this.loading = false;
          this.changeRef.detectChanges();
        });
      }),
    );
  }

  scrollToCursor(): void {
    this.followCursor = true;
    const paragraph = this.paragraphs[this.currentParagraphIndex];
    if (!this.isElementInViewport(paragraph)) {
      paragraph.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
        inline: 'nearest',
      });
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  // toggleSubtitles(): void {
  //   this.subtitles = !this.subtitles;
  // }
  //
  // toggleFollowCursor(): void {
  //   this.followCursor = !this.followCursor;
  // }

  getTitle(): string {
    if (this.useFulltext && this.hasFulltext()) {
      return this.article?.webDocument?.contentTitle;
    } else {
      return this.article?.webDocument?.title;
    }
  }

  getContent(): string {
    if (this.useFulltext && this.hasFulltext()) {
      return this.article?.webDocument?.contentRaw;
    } else {
      return this.article?.webDocument?.description;
    }
  }

  getDomain(url: string): string {
    return new URL(url).host;
  }

  async showImportModal() {}

  hasFulltext(): boolean {
    return this.article?.webDocument?.contentText?.length > 0;
  }

  createdAt(): Date {
    return new Date(this.article?.webDocument?.publishedAt);
  }

  getColorForStatus(status: GqlArticleReleaseStatus): string {
    return getColorForArticleStatus(status);
  }

  statusToString(status: GqlArticleReleaseStatus) {
    return articleStatusToString(status);
  }

  private async init() {
    this.loading = true;
    try {
      this.article = await this.articleService.findById(this.articleId);
      await this.handleReadability();
      this.bucket = this.article.bucket;
      // this.nativeFeed = this.article.nativeFeed;
      this.context = this.article.context;
      this.loading = false;
      this.changeRef.detectChanges();
    } finally {
      this.loading = false;
      this.changeRef.detectChanges();
    }
  }

  private applyStyles(): void {
    const elements =
      this.readerContent.nativeElement.querySelectorAll('article > *');
    Array.from(elements).forEach((element: any) => {
      if (element.hasChildNodes()) {
        Array.from(element.childNodes)
          .filter((child: any) => child.nodeType === 3)
          .forEach((child: any) => {
            this.convertTextToSpans(child.nodeValue).forEach((span) => {
              child.parentNode.append(span);
            });
            child.remove();
          });
      }
    });
  }

  private registerEvents() {
    this.paragraphs = Array.from(
      this.readerContent.nativeElement.querySelectorAll('.par'),
    );
    this.paragraphs.forEach((element: Node, index: number) => {
      // element.addEventListener('click', this.preparePlay(index));
    });
  }

  private isElementInViewport(el: any): boolean {
    const rect = el.getBoundingClientRect();

    return (
      rect.bottom > 0 &&
      rect.right > 0 &&
      rect.left <
        (window.innerWidth ||
          document.documentElement.clientWidth) /* or $(window).width() */ &&
      rect.bottom <
        (window.innerHeight ||
          document.documentElement.clientHeight) /* or $(window).height() */
    );
  }

  private handleReadability(): void {
    setTimeout(() => {
      this.applyStyles();
      this.registerEvents();
    }, 500);
  }

  private convertTextToSpans(text: string): HTMLSpanElement[] {
    // @ts-ignore
    return [...(text + '.').matchAll(/([^.;!?]+[.;!?]{1})/g)]
      .filter((match) => match && match[1].trim().length > 1)
      .map((match) => {
        const span: HTMLSpanElement = document.createElement('span');
        span.classList.add('par');
        span.innerText = match[1];
        return span;
      });
  }
  // private highlightParagraph(paragraphId: number) {
  //   this.paragraphs[paragraphId].classList.add('active');

  // }
  // private unhighlightParagraph(paragraphId: number) {
  //   this.paragraphs[paragraphId].classList.remove('active');

  // }
  // private updateProgress(): void {
  //   this.progress = this.currentParagraphIndex / this.paragraphs.length;
  //   const paragraph = this.paragraphs[this.currentParagraphIndex];
  //   if (!this.isElementInViewport(paragraph)) {
  //     this.lostCursor = true;
  //
  //     if (this.followCursor) {
  //       this.scrollToCursor();
  //     }
  //   }
  // }
}
