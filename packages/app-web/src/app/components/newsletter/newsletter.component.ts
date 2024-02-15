import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
} from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { environment } from '../../../environments/environment';
import { GqlPlanName } from '../../../generated/graphql';

@Component({
  selector: 'app-newsletter',
  templateUrl: './newsletter.component.html',
  styleUrls: ['./newsletter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsletterComponent {
  @Input({ required: true })
  headerText: string;
  @Input({ required: true })
  bodyText: string;
  @Input({ required: true })
  buttonText: string;

  busy = false;
  submitted = false;
  errorMessage: string;

  private emailFc = new FormControl<string>('', [
    Validators.required,
    Validators.email,
  ]);

  constructor(
    private readonly userService: UserService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async joinNow(email: string) {
    console.log('joinNow');
    this.emailFc.setValue(email);
    this.errorMessage = null;
    this.busy = true;
    this.changeRef.detectChanges();
    if (this.emailFc.valid) {
      try {
        await this.userService.createUser({
          email: this.emailFc.value,
          newsletter: true,
          plan: GqlPlanName.Waitlist,
          product: environment.product(),
        });
        this.submitted = true;
      } catch (e) {
        this.errorMessage = 'Something went wrong! Maybe try later again!';
      }
    } else {
      this.errorMessage = 'Your email looks invalid';
    }
    this.busy = false;
    this.changeRef.detectChanges();
  }
}
