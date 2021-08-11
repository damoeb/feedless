import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { GraphQLError } from 'graphql';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  constructor(public toastController: ToastController) {}

  async info(message: string) {
    return this.toast(message, 2000);
  }

  async errors(errors: ReadonlyArray<GraphQLError>) {
    return this.toast(errors.entries()[0].message, 5000, 'error');
  }

  errorFromApollo(error: Error) {
    return this.toast(error.message, 5000, 'error');
  }

  private async toast(message: string, duration: number, color: string = null) {
    const toast = await this.toastController.create({
      message,
      duration,
      color,
    });
    return toast.present();
  }
}
