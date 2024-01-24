import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { GqlVisibility } from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';


@Component({
  selector: 'app-repository-create-page',
  templateUrl: './repository-create.page.html',
  styleUrls: ['./repository-create.page.scss'],
})
export class RepositoryCreatePage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  formFg = new FormGroup({
    title: new FormControl<string>('', [Validators.required, Validators.minLength(1)]),
    description: new FormControl<string>('', []),
    visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPrivate, []),
    maxItems: new FormControl<number>(null, {
      validators: [Validators.min(2)]
    }),
    maxAgeDays: new FormControl<number>(null, {
      validators: [Validators.min(2)]
    })
  })

  constructor(
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      // this.activatedRoute.params.subscribe((params) => {
      //   this.fetchSourceSubscription(params.id);
      //   this.feedUrl = `${this.serverSettings.apiUrl}/feed/${params.id}`;
      // })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  createSink() {
    if (this.formFg.invalid) {
      return;
    }
    const form = this.formFg.value;
    this.sourceSubscriptionService.createSubscriptions({
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
            plugins: []
          }
        }
      ]
    })
  }

  protected readonly GqlVisibility = GqlVisibility;
}
