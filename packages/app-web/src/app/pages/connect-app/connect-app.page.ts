import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ConnectedApp,
  ConnectedAppService,
} from '../../services/connected-app.service';
import {
  ToastController,
  IonContent,
  IonSpinner,
  IonCard,
  IonCardContent,
  IonButtons,
  IonButton,
} from '@ionic/angular/standalone';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';


@Component({
  selector: 'app-connect-app-page',
  templateUrl: './connect-app.page.html',
  styleUrls: ['./connect-app.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FeedlessHeaderComponent,
    IonContent,
    IonSpinner,
    IonCard,
    IonCardContent,
    IonButtons,
    IonButton
],
  standalone: true,
})
export class ConnectAppPage implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly connectedAppService = inject(ConnectedAppService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly toastCtrl = inject(ToastController);
  private readonly router = inject(Router);

  loading: boolean = false;
  invalidRequest: boolean = false;
  protected connectedApp: ConnectedApp;
  private appConnectionId: string;

  async ngOnInit() {
    this.appConnectionId = this.activatedRoute.snapshot.params['link'];
    console.log(this.activatedRoute.snapshot.params);
    this.loading = true;
    try {
      if (this.appConnectionId) {
        this.connectedApp = await this.connectedAppService.findById(
          this.appConnectionId,
        );
      } else {
        this.invalidRequest = true;
      }
    } catch (e) {
      this.invalidRequest = true;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  async authorizeApp() {
    await this.connectedAppService.updateConnectedApp(
      this.appConnectionId,
      true,
    );

    await this.showToast('Authorized');
    await this.router.navigateByUrl('/');
  }

  async cancelAttempt() {
    await this.connectedAppService.deleteConnectedApp(this.appConnectionId);
    await this.showToast('Request Canceled');
    await this.router.navigateByUrl('/');
  }

  private async showToast(message: string) {
    const toast = await this.toastCtrl.create({
      message: message,
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }
}
