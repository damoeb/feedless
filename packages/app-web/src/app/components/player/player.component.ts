import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { GetElementType, Record } from '../../graphql/types';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { first } from 'lodash-es';
import { addIcons } from 'ionicons';
import { playOutline } from 'ionicons/icons';

import { IonButton, IonIcon, IonNote } from '@ionic/angular/standalone';

type Enclosure = GetElementType<Record['attachments']>;

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss'],
  imports: [IonButton, IonIcon, IonNote],
  standalone: true,
})
export class PlayerComponent {
  private readonly domSanitizer = inject(DomSanitizer);

  @Input({ required: true })
  document: Record;

  @Input()
  isPlaying: boolean;

  @Output()
  playback = new EventEmitter<void>();

  constructor() {
    addIcons({ playOutline });
  }

  hasAudioStream(document: Record): boolean {
    return document.attachments?.some((e) => e.type.startsWith('audio/'));
  }

  firstAudioStream(document: Record): SafeResourceUrl {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return this.domSanitizer.bypassSecurityTrustResourceUrl(audioStream.url);
    }
  }

  private firstAudioEnclosure(document: Record): Enclosure {
    return first(
      document.attachments.filter((e) => e.type.startsWith('audio/')),
    );
  }

  firstAudioLength(document: Record): string {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      if (audioStream.duration <= 60) {
        return `${audioStream.duration}  Sec.`;
      } else {
        return `${parseInt(`${audioStream.duration / 60}`)}  Min.`;
      }
    }
  }

  playAudio() {
    this.isPlaying = true;
    this.playback.emit();
  }
}
