import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { AuthService, ConfirmCode } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SessionService } from 'src/app/services/session.service';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { addIcons } from 'ionicons';
import { arrowForwardOutline } from 'ionicons/icons';
import {
  IonCard,
  IonCardContent,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSpinner,
} from '@ionic/angular/standalone';
import { Nullable } from '../../types';
import { min } from 'lodash-es';

type StepMode = 'enterMail' | 'enterConfirmationCode' | 'finalized';

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
    IonSpinner,
  ],
  standalone: true,
})
export class EmailLoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);

  mode: StepMode = 'enterMail';
  modeEnterMail: StepMode = 'enterMail';
  modeConfirmCode: StepMode = 'enterConfirmationCode';
  emailFc = new FormControl<string>('', [Validators.email, Validators.required]);
  private botChallengeNumber: number = 100;
  botChallengeFc = new FormControl<number>(null, [
    Validators.required,
    Validators.min(0),
    Validators.max(100),
  ]);
  botChallengeLabel: string = '';
  busy = false;
  confirmationCodeFc: FormControl<string>;
  private confirmationCodeSpec: Nullable<ConfirmCode> = null;
  errorMessage: string;

  constructor() {
    addIcons({ arrowForwardOutline });
    this.initBotChallenge();
  }

  private initBotChallenge() {
    const randomNumbers = Array.from({ length: 3 }, () => Math.floor(Math.random() * 101));

    this.botChallengeNumber = min(randomNumbers);
    this.botChallengeLabel = `Whats the smallest number of ${randomNumbers.join(', ')}`;
  }

  async initiateSession() {
    try {
      if (this.emailFc.invalid || this.busy) {
        return;
      }
      this.errorMessage = null;
      this.busy = true;
      this.changeRef.detectChanges();

      const confirmCode = await this.authService.authorizeUserViaMail(this.emailFc.value);
      this.confirmationCodeSpec = confirmCode;
      this.mode = 'enterConfirmationCode';

      const length = confirmCode.length;
      this.confirmationCodeFc = new FormControl<string>('', [
        Validators.required,
        Validators.minLength(length),
        Validators.maxLength(length),
      ]);
      this.changeRef.detectChanges();
    } catch (e) {
      console.error(e);
    } finally {
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  async sendConfirmationCode() {
    try {
      if (
        this.confirmationCodeFc.invalid ||
        this.busy ||
        !this.confirmationCodeSpec ||
        this.botChallengeFc.value !== this.botChallengeNumber
      ) {
        return;
      }

      this.busy = true;
      this.changeRef.detectChanges();

      await this.authService.sendConfirmationCode(
        this.confirmationCodeFc.value,
        this.confirmationCodeSpec.otpId
      );

      await this.handleSuccess();
    } catch (e) {
      console.error(e);
    } finally {
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  private async handleSuccess() {
    await this.sessionService.fetchSession('network-only');
    await this.router.navigateByUrl('/');
  }
}
