import { Component, EventEmitter, Input, Output } from '@angular/core';
import { GetElementType, WebDocument } from '../../graphql/types';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { first } from 'lodash-es';

type Enclosure = GetElementType<WebDocument['enclosures']>

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss'],
})
export class PlayerComponent {
  @Input({required: true})
  document: WebDocument;

  @Input()
  isPlaying: boolean;

  @Output()
  playback = new EventEmitter<void>()

  constructor(private readonly domSanitizer: DomSanitizer) {}


  hasAudioStream(document: WebDocument): boolean {
    return document.enclosures.some((e) => e.type.startsWith('audio/'));
  }

  firstAudioStream(document: WebDocument): SafeResourceUrl {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return this.domSanitizer.bypassSecurityTrustResourceUrl(audioStream.url);
    }
  }

  private firstAudioEnclosure(document: WebDocument): Enclosure {
    return first(
      document.enclosures.filter((e) => e.type.startsWith('audio/')),
    );
  }

  firstAudioLength(document: WebDocument): string {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return `${parseInt(`${audioStream.duration / 60}`)}  Min.`;
    }
  }

  playAudio() {
    this.isPlaying = true;
    this.playback.emit();
  }
}
