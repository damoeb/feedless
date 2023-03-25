import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ArticleService,
  ArticleWithContext,
  BasicContext,
} from '../../services/article.service';
import { ModalController } from '@ionic/angular';
import { Bucket } from '../../services/bucket.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { PlayerService } from '../../services/player.service';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-article-page',
  templateUrl: './article.page.html',
  styleUrls: ['./article.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ArticlePage implements OnInit {
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
  nativeFeed: BasicNativeFeed;
  context: BasicContext;

  private paragraphs: any[] = [];
  private currentParagraphIndex = 0;
  private articleId: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly playerService: PlayerService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly articleService: ArticleService
  ) {}

  ngOnInit() {
    this.useFulltext = this.profileService.useFulltext();
    this.activatedRoute.params.subscribe((params) => {
      this.articleId = params.articleId;
      this.init(params.articleId).finally(() => {
        this.playerService.pushFirst(this.article);
        this.loading = false;
        this.changeRef.detectChanges();
      });
    });
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

  toggleSubtitles(): void {
    this.subtitles = !this.subtitles;
  }

  toggleFollowCursor(): void {
    this.followCursor = !this.followCursor;
  }

  getTitle(): string {
    if (this.useFulltext && this.hasFulltext()) {
      return this.article?.content?.contentTitle;
    } else {
      return this.article?.content?.title;
    }
  }

  getContent(): string {
    if (this.useFulltext && this.hasFulltext()) {
      return this.article?.content?.contentRaw;
    } else {
      return this.article?.content?.description;
    }
  }

  getDomain(url: string): string {
    return new URL(url).host;
  }

  async showImportModal() {}

  hasFulltext(): boolean {
    return this.article?.content?.contentText?.length > 0;
  }

  createdAt(): Date {
    return new Date(this.article?.content?.publishedAt);
  }

  private async init(articleId: string) {
    this.loading = true;
    try {
      this.article = await this.articleService.findById(articleId);
      await this.handleReadability();
      this.bucket = this.article.bucket;
      this.nativeFeed = this.article.nativeFeed;
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
      this.readerContent.nativeElement.querySelectorAll('.par')
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
