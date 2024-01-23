import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { GqlAuthenticationEventMessage, GqlConfirmCode } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ProfileService } from 'src/app/services/profile.service';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-mail-code-login',
  templateUrl: './mail-code-login.component.html',
  styleUrls: ['./mail-code-login.component.scss'],
})
export class MailCodeLoginComponent implements OnDestroy {
  mode:
    | 'enterMail'
    | 'enterConfirmationCode'
    | 'finalized' = 'enterMail';
  private confirmationCodeSpec: Pick<GqlConfirmCode, 'length' | 'otpId'>;
  message: Pick<GqlAuthenticationEventMessage, 'message' | 'isError'>;
  private subscriptionHandle: { unsubscribe: () => void; closed: boolean };
  emailFc = new FormControl<string>('', [Validators.email, Validators.required]);
  busy = false;
  confirmationCodeFc: FormControl<string>;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly profileService: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  async initiateSession() {
    try {
      if (this.emailFc.invalid || this.busy) {
        return;
      }
      this.busy = true;
      this.changeRef.detectChanges();
      this.subscriptionHandle = (
        await this.authService.authorizeUserViaMail(`${this.emailFc.value}`)
      ).subscribe(async (response) => {
        const data = response.data.authViaMail;
        if (data.confirmCode) {
          this.busy = false;
          this.mode = 'enterConfirmationCode';
          this.confirmationCodeSpec = data.confirmCode;
          const length = data.confirmCode.length;
          this.confirmationCodeFc = new FormControl<string>('', [Validators.required, Validators.minLength(length), Validators.maxLength(length)])
          this.changeRef.detectChanges();
        } else if (data.authentication) {
          this.mode = 'finalized';
          await this.authService.handleAuthenticationToken(
            data.authentication.token,
          );
          await this.profileService.fetchProfile('network-only');
          await this.router.navigateByUrl('/');
          this.changeRef.detectChanges();
          this.unsubscribe();
        } else if (data.message) {
          this.message = data.message;
          this.changeRef.detectChanges();
        } else {
          console.log('ws event', response.data.authViaMail);
        }
      });
    } catch (e) {
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  sendConfirmationCode() {
    try {
      if (this.confirmationCodeFc.invalid || this.busy) {
        return;
      }

      this.busy = true;
      this.changeRef.detectChanges();

      return this.authService.sendConfirmationCode(
        this.confirmationCodeFc.value,
        this.confirmationCodeSpec.otpId,
      );
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
}
