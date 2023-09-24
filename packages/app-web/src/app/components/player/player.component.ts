import { ChangeDetectorRef, Component } from '@angular/core';
import { Platform } from '@ionic/angular';
import { Article } from '../../graphql/types';

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss'],
})
export class PlayerComponent {
  locale = 'de-AT';

  playing = false;
  progress = 0;
  scrollPosition = 0.5;
  currentTextTrack: string;
  followCursor: boolean;

  article: Article;
  useFulltext: boolean;

  private paragraphs: any[] = [];
  private currentParagraphIndex = 0;
  private rate = 1;
  private tts = {
    stop: () => Promise.resolve(),
    speak: (p: { rate: number; text: string; locale: string }) =>
      Promise.resolve(),
  };

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly platform: Platform,
  ) {}

  stop(event?: MouseEvent): Promise<any> {
    console.log('stop');
    if (event) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
    this.playing = false;
    return this.tts.stop();
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

  togglePlayback(): Promise<any> {
    if (this.playing) {
      return this.stop();
    } else {
      return this.play(this.currentParagraphIndex);
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

  toggleSubtitles(): void {}

  toggleFollowCursor(): void {
    this.followCursor = !this.followCursor;
  }

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

  hasFulltext(): boolean {
    return this.article?.webDocument?.contentText?.length > 0;
  }

  createdAt(): Date {
    return new Date(this.article?.webDocument?.publishedAt);
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
      // this.lostCursor = true;

      if (this.followCursor) {
        this.scrollToCursor();
      }
    }
  }
}
