import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
  loading = true;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly apollo: Apollo,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  async ngOnInit() {
    const token = this.activatedRoute.snapshot.params.token;
    if (token) {
      this.authService.handleAuthToken(token);
      await this.router.navigateByUrl('/');
    }
    this.loading = false;
  }

  async initiateOauth() {
    const redirect = await this.apollo
      .mutate<any>({
        mutation: gql`
          mutation {
            getOauthRedirect
          }
        `,
      })
      .toPromise()
      .then(({ data }) => data.getOauthRedirect as string);
    return this.router.navigateByUrl(redirect);
  }
}
