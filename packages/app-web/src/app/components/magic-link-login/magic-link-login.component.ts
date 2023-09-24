import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import {
  GqlAuthenticationEventMessage,
  GqlConfirmCode,
} from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ProfileService } from 'src/app/services/profile.service';

@Component({
  selector: 'app-magic-link-login',
  templateUrl: './magic-link-login.component.html',
  styleUrls: ['./magic-link-login.component.scss'],
})
export class MagicLinkLoginComponent implements OnDestroy {
  mode:
    | 'enterMail'
    | 'waitForMagicLink'
    | 'enterConfirmationCode'
    | 'finalized' = 'enterMail';
  confirmationCodeSpec: Pick<GqlConfirmCode, 'length' | 'otpId'>;
  message: Pick<GqlAuthenticationEventMessage, 'message' | 'isError'>;
  private subscriptionHandle: { unsubscribe: () => void; closed: boolean };

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly profileService: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  async initiateSession(email: string | number) {
    this.mode = 'waitForMagicLink';
    this.changeRef.detectChanges();
    this.subscriptionHandle = (
      await this.authService.requestAuthForUser(`${email}`)
    ).subscribe(async (response) => {
      const data = response.data.authViaMail;
      if (data.confirmCode) {
        this.mode = 'enterConfirmationCode';
        this.confirmationCodeSpec = data.confirmCode;
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
  }

  sendConfirmationCode(value: string | number) {
    const confirmationCode = `${value}`;
    if (confirmationCode.length > 3) {
      return this.authService.sendConfirmationCode(
        confirmationCode,
        this.confirmationCodeSpec.otpId,
      );
    }
  }

  private unsubscribe(): void {
    if (this.subscriptionHandle && !this.subscriptionHandle.closed) {
      this.subscriptionHandle.unsubscribe();
    }
  }
}
