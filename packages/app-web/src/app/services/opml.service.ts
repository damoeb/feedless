import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { FileService } from './file.service';

export interface Outline {
  title: string;
  text?: string;
  xmlUrl?: string;
  htmlUrl?: string;
  outlines?: Outline[];
}

@Injectable({
  providedIn: 'root',
})
export class OpmlService {
  constructor(
    private readonly toastCtrl: ToastController,
    private readonly fileService: FileService,
  ) {}

  async convertOpmlToJson(uploadEvent: Event): Promise<Outline[]> {
    const data = this.fileService.uploadAsText(uploadEvent);
    const parser = new DOMParser();
    const doc = parser.parseFromString(String(data), 'application/xml');
    const errorNode = doc.querySelector('parsererror');
    if (errorNode) {
      console.log(errorNode);
      const toast = await this.toastCtrl.create({
        message: 'Parsing failed',
        duration: 3000,
        color: 'danger',
      });
      await toast.present();
      throw new Error(`${errorNode}`);
    } else {
      const groups = Array.from(
        doc.documentElement.querySelectorAll('body>outline'),
      );
      return groups.map((group) => this.parseOutline(group));
    }
  }

  private parseOutline(group: Element): Outline {
    return {
      title: group.getAttribute('title'),
      text: group.getAttribute('text'),
      xmlUrl: group.getAttribute('xmlUrl'),
      htmlUrl: group.getAttribute('htmlUrl'),
      outlines: Array.from(group.children).map((child) =>
        this.parseOutline(child),
      ),
    };
  }
}
