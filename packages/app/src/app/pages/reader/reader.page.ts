import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import {
  ActionSheetController,
  ModalController,
  Platform,
} from '@ionic/angular';
import * as Readability from '@mozilla/readability/Readability';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';

import { ReadabilityService } from '../../services/readability.service';
import { GqlArticle, GqlArticleRef } from '../../../generated/graphql';
import { IntegratePage } from '../integrate/integrate.page';
import { ArticleService } from '../../services/article.service';
import { ActivatedRoute } from '@angular/router';

export interface Readability {
  content: string;
  title: string;
}

@Component({
  selector: 'app-reader',
  templateUrl: './reader.page.html',
  styleUrls: ['./reader.page.scss'],
})
export class ReaderPage implements OnInit {
  public error: boolean;
  public errorMsg: any;
  public loading = false;

  articleRef: GqlArticleRef;
  article: GqlArticle;

  public locale: string = 'de-AT';
  @ViewChild('narrator', { static: true }) readerContent: ElementRef;
  private paragraphs: any[] = [];
  private currentParagraphIndex = 0;
  private rate = 1;
  public playing: boolean = false;
  public progress = 0;
  public scrollPosition = 0.5;
  public currentTextTrack: string;
  public subtitles: boolean;
  public followCursor: boolean;
  public lostCursor: boolean;
  public title: string;
  public content: string;
  betterRead: boolean;
  canReadOutLoud: boolean;

  constructor(
    private readonly readabilityService: ReadabilityService,
    private readonly actionSheetController: ActionSheetController,
    private readonly tts: TextToSpeech,
    private readonly modalController: ModalController,
    private readonly platform: Platform,
    private readonly activatedRoute: ActivatedRoute,
    private readonly articleService: ArticleService
  ) {}

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((queryParams) => {
      console.log(`url ` + queryParams.url);
    });
    this.activatedRoute.params.subscribe((params) => {
      console.log(`articleId ` + params.id);
      if (params.id) {
        this.articleService.findById(params.id).subscribe((response) => {
          this.articleRef = response.data.findFirstArticleRef;
          this.article = this.articleRef.article;
          this.title = this.articleRef.article.title;
          this.content = this.articleService.removeXmlMetatags(
            this.articleRef.article.content_text
          );
        });
      }
    });
    this.canReadOutLoud =
      this.platform.is('android') || this.platform.is('ios');
  }

  async showSettings() {
    let options = {
      header: 'Options',
      buttons: [
        {
          text: 'More Like This',
          icon: 'magnet-outline',
          handler: () => {
            console.log('Delete clicked');
          },
        },
        {
          text: 'Better Read',
          icon: 'contrast-outline',
          handler: () => {
            this.toggleBetterRead();
          },
        },
        {
          text: 'Full Text',
          icon: 'book-outline',
          handler: () => {
            this.renderFulltext();
          },
        },
      ],
    };

    if (this.canReadOutLoud) {
      options.buttons.push({
        text: 'Read Out Loud',
        icon: 'volume-medium-outline',
        handler: () => {
          this.togglePlayback();
        },
      });
    }

    const actionSheet = await this.actionSheetController.create(options);
    await actionSheet.present();
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

  public async stop(event?: MouseEvent): Promise<any> {
    console.log('stop');
    if (event) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
    this.playing = false;
    return this.tts.stop();
  }

  private static isElementInViewport(el: any): boolean {
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

  public canNext(): boolean {
    return this.currentParagraphIndex + 1 < this.paragraphs.length;
  }
  public canPrevious(): boolean {
    return this.currentParagraphIndex - 1 >= 0;
  }

  public next(): void {
    if (this.canNext()) {
      this.unhighlightParagraph(this.currentParagraphIndex);
      this.highlightParagraph(this.currentParagraphIndex + 1);
      this.currentParagraphIndex = this.currentParagraphIndex + 1;
      this.updateProgress();
    }
  }

  public previous(): void {
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
    if (this.platform.is('android') || this.platform.is('ios')) {
      return this.tts.speak({ text, locale, rate: this.rate });
    } else {
      return new Promise<any>((resolve) => {
        setTimeout(resolve, 2000);
      });
    }
  }

  private handleReadability(readability: Readability): void {
    if (readability) {
      this.content = readability.content;
      setTimeout(() => {
        this.applyStyles();
        this.registerEvents();
      }, 500);
    }
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

  public togglePlayback(): Promise<any> {
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
    if (!ReaderPage.isElementInViewport(paragraph)) {
      this.lostCursor = true;

      if (this.followCursor) {
        this.scrollToCursor();
      }
    }
  }

  scrollToCursor(): void {
    this.followCursor = true;
    const paragraph = this.paragraphs[this.currentParagraphIndex];
    if (!ReaderPage.isElementInViewport(paragraph)) {
      paragraph.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
        inline: 'nearest',
      });
    }
  }

  public toggleSubtitles(): void {
    this.subtitles = !this.subtitles;
  }

  public toggleFollowCursor(): void {
    this.followCursor = !this.followCursor;
  }

  toggleBetterRead() {
    this.betterRead = !this.betterRead;
    if (this.betterRead) {
      this.content = this.toBetterRead();
    } else {
      this.content = this.articleRef.article.content_text;
    }
  }

  private toBetterRead() {
    const re = /^.{2,}/i;
    return this.articleRef.article.content_text
      .split(' ')
      .map((word) => {
        if (re.test(word)) {
          return `<b>${word.substring(0, word.length / 2)}</b>${word.substring(
            word.length / 2
          )}`;
        }
        return word;
      })
      .join(' ');
  }

  toggleFav() {
    this.articleRef.favored = !this.articleRef.favored;
  }

  renderFulltext() {
    console.log('Reader using url', this.articleRef.article.url);
    this.readabilityService
      .get(this.articleRef.article.url)
      .then((readability: Readability) => {
        this.title = readability.title;
        this.handleReadability(readability);
        console.log('Extracted readability', readability);
      })
      .catch((error) => {
        this.error = true;
        this.errorMsg = error;
        console.error(error);
      })
      .finally(() => {
        this.loading = false;
      });
  }

  getEnclosure() {
    const enclosure = JSON.parse(this.articleRef.article.enclosure);
    return `<audio src="${enclosure.url}" controls></audio>`;
  }

  async integrateArticle() {
    const modal = await this.modalController.create({
      component: IntegratePage,
      backdropDismiss: false,
      componentProps: {
        article: this.articleRef.article,
      },
    });
    // modal.onDidDismiss<GqlDiscoveredFeed>().then((response) => {
    // });

    await modal.present();
  }
}
