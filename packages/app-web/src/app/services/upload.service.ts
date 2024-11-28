import { inject, Injectable } from '@angular/core';
import { AlertController, ToastController } from '@ionic/angular/standalone';
import { Record } from '../graphql/types';
import { RecordService } from './record.service';

export type UploadFileHandler = {
  mimeTypes: string[];
  handleData: (file: File, data: URL | string) => Promise<Record>;
  binaryContent: boolean;
};

@Injectable({
  providedIn: 'root',
})
export class UploadService {
  private readonly recordService = inject(RecordService);
  private readonly alertController = inject(AlertController);
  private readonly toastController = inject(ToastController);

  private fileHandlers: UploadFileHandler[];

  constructor() {
    this.fileHandlers = this.createFileHandlers();
  }

  uploadFile(uploadEvent: Event): void {
    Array.from((uploadEvent.target as any).files as any).forEach(
      (file: any) => {
        console.log('upload', file);
        const handler = this.fileHandlers.find((fileHandler) => {
          return fileHandler.mimeTypes.some((mimeTypeRE) => {
            const matches = file.type.match(new RegExp(mimeTypeRE));
            return matches && matches.length > 0;
          });
        });

        if (!handler) {
          // this.core.showError(`Your file '${file.name}' has MIME type '${file.type}',` +
          //   ` should be one of ${this.getAcceptedMimes()}.`, 'Unsupported MIME Type');
          throw new Error(`No handler available for ${file.type}`);
        }

        const reader = new FileReader();

        reader.onloadend = async (event) => {
          const data: ArrayBuffer | string = (event.target as any).result;
          await handler.handleData(file, data as any);
        };

        // Read in the image file as a data URL.
        if (handler.binaryContent) {
          reader.readAsDataURL(file);
        } else {
          reader.readAsText(file);
        }
      },
    );
  }

  getAcceptedMimeTypes(): string {
    return this.fileHandlers
      .map((fileHandler) => fileHandler.mimeTypes)
      .reduce((acc, val) => acc.concat(val), [])
      .join(', ');
  }

  private createFileHandlers(): UploadFileHandler[] {
    return [
      {
        mimeTypes: ['image/png', 'image/jpeg', 'image/gif'],
        handleData: async (file, dataUrl) => {
          console.log('upload image');

          // const note = await this.core.createBinaryNote(file, dataUrl, 'image');
          const caption = await this.askForCaption(file);
          return this.recordService.createRecordFromUpload(
            caption,
            file,
            dataUrl,
          );
        },
        binaryContent: true,
      },
      {
        mimeTypes: ['application/pdf'],
        handleData: async (file, dataUrl) => {
          console.log('upload pdf');
          const caption = await this.askForCaption(file);
          return this.recordService.createRecordFromUpload(
            caption,
            file,
            dataUrl,
          );
        },
        binaryContent: true,
      },
      {
        mimeTypes: ['text/*'],
        handleData: async (file, data) => {
          const caption = await this.askForCaption(file);
          throw new Error('not implemented');
          // return this.recordService.createRecords(caption, data);
        },
        binaryContent: false,
      },
    ];
  }

  private async askForCaption(file: File) {
    const cancel = 'cancel';
    const alert = await this.alertController.create({
      header: 'Enter a name',
      buttons: [
        {
          text: 'Cancel',
          role: cancel,
        },
        {
          text: 'Save',
        },
      ],
      inputs: [
        {
          type: 'text',
          name: 'title',
          value: file.name,
          placeholder: 'Type here',
        },
      ],
    });

    await alert.present();
    const { data, role } = await alert.onDidDismiss();
    if (role === cancel) {
      const toast = await this.toastController.create({
        message: 'Upload aborted',
      });
      await toast.present;

      throw new Error('cancel');
    }
    return data.values.title;
  }
}
