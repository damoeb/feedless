import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';
import { ModalController, Platform } from '@ionic/angular';
import * as Readability from '@mozilla/readability/Readability';
import { ReadabilityService } from '../../services/readability.service';

export interface Readability {
  content: string;
  title: string;
}

@Component({
  selector: 'app-reader',
  templateUrl: './reader.component.html',
  styleUrls: ['./reader.component.scss'],
})
export class ReaderComponent implements OnInit {
  public error: boolean;
  public errorMsg: any;
  public loading = false;

  public content: string;
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

  constructor(
    private readabilityService: ReadabilityService,
    private tts: TextToSpeech,
    private modalController: ModalController,
    private platform: Platform
  ) {}

  ngOnInit() {
    this.loading = true;

    // todo mag enable
    // this.state.getUrl().subscribe((url) => {
    //   console.log('Reader using url', url);
    //   this.readabilityService
    //     .get(url)
    //     .then((readability: Readability) => {
    //       this.title = readability.title;
    //       this.handleReadability(readability);
    //       console.log('Extracted readability', readability);
    //     })
    //     .catch((error) => {
    //       this.error = true;
    //       this.errorMsg = error;
    //       console.error(error);
    //     })
    //     .finally(() => {
    //       this.loading = false;
    //     });
    // });
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

  public stop(event?: MouseEvent): Promise<any> {
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
    if (!ReaderComponent.isElementInViewport(paragraph)) {
      this.lostCursor = true;

      if (this.followCursor) {
        this.scrollToCursor();
      }
    }
  }

  scrollToCursor(): void {
    this.followCursor = true;
    const paragraph = this.paragraphs[this.currentParagraphIndex];
    if (!ReaderComponent.isElementInViewport(paragraph)) {
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
}
