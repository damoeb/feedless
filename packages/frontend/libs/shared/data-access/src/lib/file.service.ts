import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class FileService {
  async uploadAsText(uploadEvent: Event): Promise<string> {
    const input = uploadEvent.target;

    if (!(input instanceof HTMLInputElement)) {
      throw new Error('Event target is not an input element');
    }

    const file = input.files?.[0];

    if (!file) {
      throw new Error('No file selected');
    }

    return new Promise<string>((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = () => {
        resolve(reader.result as string);
      };

      reader.onerror = () => {
        reject(reader.error ?? new Error('File reading failed'));
      };

      reader.readAsText(file);
    });
  }
}
