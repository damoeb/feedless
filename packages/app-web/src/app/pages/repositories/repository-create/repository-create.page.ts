import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { GqlVisibility } from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Authentication, AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-repository-create-page',
  templateUrl: './repository-create.page.html',
  styleUrls: ['./repository-create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryCreatePage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  protected readonly GqlVisibility = GqlVisibility;
  loading = false;

  formFg = new FormGroup({
    title: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(50),
    ]),
    description: new FormControl<string>('', []),
    visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPrivate, []),
    maxItems: new FormControl<number>(null, {
      validators: [Validators.min(2)],
    }),
    maxAgeDays: new FormControl<number>(null, {
      validators: [Validators.min(2)],
    }),
  });
  errorMessage: string;
  private authorization: Authentication;
  showErrors = false;

  constructor(
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.authService.authorizationChange().subscribe((authorization) => {
        this.authorization = authorization;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async createRepository() {
    this.showErrors = false;
    if (this.formFg.invalid) {
      this.showErrors = true;
      this.changeRef.detectChanges();
      return;
    }
    this.errorMessage = null;
    this.loading = true;
    this.changeRef.detectChanges();

    if (this.authorization.loggedIn) {
      try {
        const form = this.formFg.value;
        const subs = await this.sourceSubscriptionService.createSubscriptions({
          subscriptions: [
            {
              sources: [],
              sinkOptions: {
                title: form.title,
                description: form.description,
                retention: {
                  maxItems: form.maxItems,
                  maxAgeDays: form.maxAgeDays,
                },
                visibility: form.visibility,
                plugins: [],
              },
            },
          ],
        });

        await this.router.navigateByUrl(`/repositories/${subs[0].id}`);
      } catch (e) {
        this.errorMessage = e.message;
      }
    } else {
      await new Promise((resolve) => setTimeout(resolve, 2000));
      this.errorMessage = 'You need to be logged in';
    }

    this.loading = false;
    this.changeRef.detectChanges();
  }
}
