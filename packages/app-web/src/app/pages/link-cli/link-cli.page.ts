import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-terms',
  templateUrl: './link-cli.page.html',
  styleUrls: ['./link-cli.page.scss'],
})
export class LinkCliPage implements OnInit {
  otpId: string;
  submitted = false;
  constructor(
    private readonly authService: AuthService,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((params) => {
      this.otpId = params.id;
    });
  }

  sendConfirmationCode(value: string | number) {
    const confirmationCode = `${value}`;
    if (confirmationCode.length > 3) {
      return this.authService
        .sendConfirmationCode(confirmationCode, this.otpId)
        .then((response) => {
          console.log(response);
          this.submitted = true;
        });
    }
  }
}
