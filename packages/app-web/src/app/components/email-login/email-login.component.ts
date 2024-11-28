import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { GqlConfirmCode } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SessionService } from 'src/app/services/session.service';
import {
  FormControl,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { addIcons } from 'ionicons';
import { arrowForwardOutline } from 'ionicons/icons';
import {
  IonCard,
  IonCardContent,
  IonList,
  IonItem,
  IonInput,
  IonLabel,
  IonIcon,
  IonSpinner,
} from '@ionic/angular/standalone';


@Component({
  selector: 'app-email-login',
  templateUrl: './email-login.component.html',
  styleUrls: ['./email-login.component.scss'],
  imports: [
    IonCard,
    IonCardContent,
    IonList,
    FormsModule,
    IonItem,
    IonInput,
    ReactiveFormsModule,
    IonLabel,
    IonIcon,
    IonSpinner
],
  standalone: true,
})
export class EmailLoginComponent implements OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);

  mode: 'enterMail' | 'enterConfirmationCode' | 'finalized' = 'enterMail';
  emailFc = new FormControl<string>('', [
    Validators.email,
    Validators.required,
  ]);
  busy = false;
  confirmationCodeFc: FormControl<string>;
  private confirmationCodeSpec: Pick<GqlConfirmCode, 'length' | 'otpId'>;
  private subscriptionHandle: { unsubscribe: () => void; closed: boolean };
  errorMessage: string;

  constructor() {
    addIcons({ arrowForwardOutline });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  async initiateSession() {
    try {
      if (this.emailFc.invalid || this.busy) {
        return;
      }
      this.errorMessage = null;
      this.busy = true;
      this.changeRef.detectChanges();
      this.subscriptionHandle = (
        await this.authService.authorizeUserViaMail(this.emailFc.value)
      ).subscribe(
        async (response) => {
          console.log('response', response);
          const data = response.data.authViaMail;
          if (data.confirmCode) {
            this.busy = false;
            this.mode = 'enterConfirmationCode';
            this.confirmationCodeSpec = data.confirmCode;
            const length = data.confirmCode.length;
            this.confirmationCodeFc = new FormControl<string>('', [
              Validators.required,
              Validators.minLength(length),
              Validators.maxLength(length),
            ]);
            this.changeRef.detectChanges();
          } else if (data.authentication) {
            this.mode = 'finalized';
            await this.authService.handleAuthenticationToken(
              data.authentication.token,
            );
            await this.handleSuccess();
            this.changeRef.detectChanges();
            this.unsubscribe();
          } else {
            console.log('ws event', response.data.authViaMail);
          }
        },
        (e) => {
          console.error('caught', e.message);
          this.errorMessage = e.message;
          this.busy = false;
          this.changeRef.detectChanges();
        },
      );
    } catch (e) {
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  async sendAuthCode() {
    try {
      if (this.confirmationCodeFc.invalid || this.busy) {
        return;
      }

      this.busy = true;
      this.changeRef.detectChanges();

      await this.authService.sendConfirmationCode(
        this.confirmationCodeFc.value,
        this.confirmationCodeSpec.otpId,
      );

      await this.handleSuccess();
    } catch (e) {
      console.error(e);
    } finally {
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  private unsubscribe(): void {
    if (this.subscriptionHandle && !this.subscriptionHandle.closed) {
      this.subscriptionHandle.unsubscribe();
    }
  }

  private async handleSuccess() {
    await this.sessionService.fetchSession('network-only');
    await this.router.navigateByUrl('/');
  }
}
