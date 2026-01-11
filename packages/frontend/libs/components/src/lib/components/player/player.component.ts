import { Component, inject, input, output, PLATFORM_ID } from '@angular/core';
import { GetElementType, Record } from '@feedless/graphql-api';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { first } from 'lodash-es';
import { addIcons } from 'ionicons';
import { playOutline } from 'ionicons/icons';

import { IonButton, IonNote } from '@ionic/angular/standalone';
import { Nullable } from '@feedless/core';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

type Enclosure = GetElementType<Record['attachments']>;

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss'],
  imports: [IonButton, IconComponent, IonNote],
  standalone: true,
})
export class PlayerComponent {
  private readonly domSanitizer = inject(DomSanitizer);

  readonly document = input.required<Record>();

  protected isPlaying = false;

  readonly playback = output<void>();
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ playOutline });
    }
  }

  hasAudioStream(document: Record): boolean {
    return document.attachments?.some((e) => e.type.startsWith('audio/'));
  }

  firstAudioStream(document: Record): Nullable<SafeResourceUrl> {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return this.domSanitizer.bypassSecurityTrustResourceUrl(audioStream.url);
    }
    return undefined;
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
    return '';
  }

  playAudio() {
    this.isPlaying = true;
    this.playback.emit();
  }
}
