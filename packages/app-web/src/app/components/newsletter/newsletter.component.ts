import { Component, Input } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-newsletter',
  templateUrl: './newsletter.component.html',
  styleUrls: ['./newsletter.component.scss'],
})
export class NewsletterComponent {
  @Input({ required: true })
  headerText: string;
  @Input({ required: true })
  bodyText: string;
  @Input({ required: true })
  buttonText: string;

  emailFc = new FormControl<string>('', [
    Validators.required,
    Validators.email,
  ]);

  constructor(private readonly userService: UserService) {}

  async joinNow($event: string) {
    if (this.emailFc.valid) {
      await this.userService.createUser({
        email: this.emailFc.value,
        waitList: true,
        newsletter: true,
        product: environment.product(),
      });
    }
  }
}
