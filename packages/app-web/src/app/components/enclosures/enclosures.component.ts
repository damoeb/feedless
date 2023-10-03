import { Component, Input } from '@angular/core';
import { BasicEnclosure } from '../../graphql/types';

@Component({
  selector: 'app-enclosures',
  templateUrl: './enclosures.component.html',
  styleUrls: ['./enclosures.component.scss'],
})
export class EnclosuresComponent {
  @Input()
  enclosures: BasicEnclosure[];
  // isAudio: boolean;
  // isDownload: boolean;
  // isVideo: boolean;

  constructor() {}

  // ngOnInit() {
  //   // const mainType = this.enclosure.type?.toLowerCase() || '';
  //   // this.isAudio = mainType.indexOf('audio') > -1;
  //   // this.isVideo = mainType.indexOf('video') > -1;
  //   // this.isDownload = !this.isAudio && !this.isVideo;
  // }

  // formatDurationOrSize(enclosure: BasicEnclosure): string {
  //   if (enclosure.duration) {
  //     return this.formatDuration(enclosure);
  //   }
  //   if (enclosure.size) {
  //     return this.formatSize(enclosure);
  //   }
  // }

  private formatSize(enclosure: BasicEnclosure): string {
    const kb = enclosure.size / 1000;
    if (kb < 1000) {
      return `${kb.toFixed(0)}kB`;
    }
    const mb = kb / 1000;
    return `${mb.toFixed(1)}MB`;
  }

  private formatDuration(enclosure: BasicEnclosure): string {
    const sec = enclosure.duration;
    if (sec < 60) {
      return `${sec.toFixed(0)}s`;
    }
    const min = sec / 60;
    return `${min.toFixed(0)}min`;
  }
}
