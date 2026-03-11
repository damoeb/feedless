import { Component, inject, OnInit } from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterModule,
  RouterOutlet,
} from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { AuthService, SessionService } from '@feedless/components';

@Component({
  imports: [RouterModule, RouterOutlet, IonApp],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly sessionService = inject(SessionService);

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(async (queryParams) => {
      if (queryParams['token']) {
        console.log('with token');
        await this.authService.handleAuthenticationToken(queryParams['token']);
        await this.router.navigate([], {
          queryParams: {
            signup: null,
            token: null,
          },
          queryParamsHandling: 'merge',
        });
      } else {
        console.log('without token');
        await this.sessionService.fetchSession();
      }
    });
  }
  protected title = 'upcoming';
}
