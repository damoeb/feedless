import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article, ArticleService } from '../../services/article.service';
import { ModalController, Platform } from '@ionic/angular';
import { ImportArticleComponent, ImportArticleComponentProps } from '../../components/import-article/import-article.component';
import { SettingsService } from '../../services/settings.service';
import { ModalDismissal } from '../../app.module';

@Component({
  selector: 'app-bucket',
  templateUrl: './article.page.html',
  styleUrls: ['./article.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticlePage implements OnInit {
  locale = 'de-AT';
  @ViewChild('narrator', { static: true })
  readerContent: ElementRef;

  private paragraphs: any[] = [];
  private currentParagraphIndex = 0;
  private rate = 1;
  playing = false;
  progress = 0;
  scrollPosition = 0.5;
  currentTextTrack: string;
  subtitles: boolean;
  followCursor: boolean;
  lostCursor: boolean;

  loadingArticle: boolean;
  renderFulltext = false;
  article: Article;

  private tts = {
    stop: () => Promise.resolve(),
    speak: (p: { rate: number; text: string; locale: string }) =>
      Promise.resolve(),
  };
  bucketId: string;
  private articleId: string;
  useFulltext: boolean;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    private readonly settingsService: SettingsService,
    private readonly platform: Platform,
    private readonly articleService: ArticleService
  ) {}

  ngOnInit() {
    this.useFulltext = this.settingsService.useFulltext();
    this.activatedRoute.params.subscribe((params) => {
      this.bucketId = params.id;
      this.articleId = params.articleId;
      Promise.all([this.initArticle(params.articleId)]).then(() => {
        this.changeRef.detectChanges();
      });
    });
  }

  private async initArticle(articleId: string) {
    console.log('initArticle', articleId);
    this.loadingArticle = true;
    try {
      this.article = await this.articleService.findById(articleId);
      this.handleReadability();
    } finally {
      this.loadingArticle = false;
      this.changeRef.detectChanges();
    }
  }

  toggleFulltext(event: any) {
    this.renderFulltext = event.detail.checked;
    this.changeRef.detectChanges();
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
      element.addEventListener('click', this.preparePlay(index));
    });
  }

  private preparePlay(index: number) {
    return (event) => {
      if (this.playing) {
        this.stop(event);
      } else {
        this.play(index, event);
      }
    };
  }

  stop(event?: MouseEvent): Promise<any> {
    console.log('stop');
    if (event) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
    this.playing = false;
    return this.tts.stop();
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

  canNext(): boolean {
    return this.currentParagraphIndex + 1 < this.paragraphs.length;
  }
  canPrevious(): boolean {
    return this.currentParagraphIndex - 1 >= 0;
  }

  next(): void {
    if (this.canNext()) {
      this.unhighlightParagraph(this.currentParagraphIndex);
      this.highlightParagraph(this.currentParagraphIndex + 1);
      this.currentParagraphIndex = this.currentParagraphIndex + 1;
      this.updateProgress();
    }
  }

  previous(): void {
    if (this.canPrevious()) {
      this.unhighlightParagraph(this.currentParagraphIndex);
      this.highlightParagraph(this.currentParagraphIndex - 1);
      this.currentParagraphIndex = this.currentParagraphIndex - 1;
      this.updateProgress();
    }
  }

  private async play(index: number = 0, event?: MouseEvent) {
    // console.log('play');
    if (event) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
    this.playing = true;
    if (!this.canNext()) {
      await this.stop();
      return;
    }
    this.unhighlightParagraph(this.currentParagraphIndex);
    this.currentParagraphIndex = index;
    this.updateProgress();
    this.highlightParagraph(index);

    const paragraph = this.paragraphs[index];
    const text = paragraph.innerText;
    this.currentTextTrack = text;

    this.read(text, this.locale).then(() => {
      if (this.playing) {
        this.play(index + 1);
      } else {
        console.log('Aborting read');
      }
    });
  }

  private read(text: string, locale: string): Promise<any> {
    if (this.platform.is('mobile')) {
      return this.tts.speak({ text, locale, rate: this.rate });
    } else {
      return new Promise<any>((resolve) => {
        setTimeout(resolve, 2000);
      });
    }
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

  togglePlayback(): Promise<any> {
    if (this.playing) {
      return this.stop();
    } else {
      return this.play(this.currentParagraphIndex);
    }
  }

  private highlightParagraph(paragraphId: number) {
    this.paragraphs[paragraphId].classList.add('active');
  }

  private unhighlightParagraph(paragraphId: number) {
    this.paragraphs[paragraphId].classList.remove('active');
  }

  private updateProgress(): void {
    this.progress = this.currentParagraphIndex / this.paragraphs.length;
    const paragraph = this.paragraphs[this.currentParagraphIndex];
    if (!this.isElementInViewport(paragraph)) {
      this.lostCursor = true;

      if (this.followCursor) {
        this.scrollToCursor();
      }
    }
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
    if (this.useFulltext) {
      return this.article?.content?.contentTitle;
    } else {
      return this.article?.content?.title;
    }
  }

  getText(): string {
    if (this.useFulltext) {
      return this.article?.content?.contentRaw;
    } else {
      return this.article?.content?.description;
    }
  }

  async showImportModal() {
    const componentProps: ImportArticleComponentProps = {
      articleId: this.articleId
    };
    const modal = await this.modalCtrl.create({
      component: ImportArticleComponent,
      componentProps,
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
  }
}
