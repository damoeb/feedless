import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../../services/profile.service';
import { debounce, interval, merge, Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../../components/embedded-website/embedded-website.component';
import { BoundingBox, XyPosition } from '../../../components/embedded-image/embedded-image.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeActionInput,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapeEmitInput,
  GqlScrapeResponse,
  GqlViewPort,
  GqlWebDocumentField,
  GqlXyPosition
} from '../../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';
import { ItemReorderEventDetail, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../../services/scrape.service';
import { ScrapedElement } from '../../../graphql/types';
import { Maybe } from 'graphql/jsutils/Maybe';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { fixUrl, isValidUrl } from '../../../pages/getting-started/getting-started.page';
import { Authentication, AuthService } from '../../../services/auth.service';
import { TypedFormGroup } from '../../../components/scrape-source/scrape-source.component';
import { Router } from '@angular/router';
import { ProductConfig, ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-visual-diff-list',
  templateUrl: './subscriptions.page.html',
  styleUrls: ['./subscriptions.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  busy = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {
  }

  ngOnInit() {
    this.subscriptions.push();

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
