import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AuthService } from '../../services/auth.service';
import {
  GqlAuthenticationEventMessage,
  GqlConfirmCode,
} from '../../../generated/graphql';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePage implements OnInit, OnDestroy {
  email: string;
  mode:
    | 'enterMail'
    | 'waitForMagicLink'
    | 'enterConfirmationCode'
    | 'finalized' = 'enterMail';
  confirmationCodeSpec: Pick<GqlConfirmCode, 'length' | 'otpId'>;
  confirmationCode: string;
  message: Pick<GqlAuthenticationEventMessage, 'message' | 'isError'>;
  private subscriptionHandle: { unsubscribe: () => void; closed: boolean };

  constructor(
    private readonly authService: AuthService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      this.mode = 'finalized';
    }
  }

  initiateSession() {
    this.mode = 'waitForMagicLink';
    this.changeRef.detectChanges();
    this.subscriptionHandle = this.authService
      .requestAuthForUser(this.email)
      .subscribe((response) => {
        const data = response.data.authViaMail;
        if (data.confirmCode) {
          this.mode = 'enterConfirmationCode';
          this.confirmationCodeSpec = data.confirmCode;
          this.changeRef.detectChanges();
        } else if (data.authentication) {
          this.mode = 'finalized';
          this.authService.handleAuthentication(data.authentication);
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

  sendConfirmationCode() {
    return this.authService.sendConfirmationCode(
      this.confirmationCode,
      this.confirmationCodeSpec.otpId
    );
  }

  private unsubscribe(): void {
    if (this.subscriptionHandle && !this.subscriptionHandle.closed) {
      this.subscriptionHandle.unsubscribe();
    }
  }
}
