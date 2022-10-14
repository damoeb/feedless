import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastController } from '@ionic/angular';

@Injectable()
export class HttpErrorInterceptorService implements HttpInterceptor {
  constructor(private readonly toastCtrl: ToastController) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler):   Observable<HttpEvent<any>> {
    return next.handle(req);
      // todo mag fix
      // .pipe(
      // // We use the map operator to change the data from the response
      // catchError(async (err, caught) => {
      //   const toast = await this.toastCtrl.create({
      //     buttons: [
      //       {
      //         role: 'cancel',
      //         text: 'Close'
      //       }
      //     ],
      //     message: err,
      //     position: 'bottom'
      //   });
      //   await toast.present();
      //   return null
      // })
    // );
   }
}
