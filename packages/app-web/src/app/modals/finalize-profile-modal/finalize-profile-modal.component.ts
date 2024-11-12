import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import {
  needsPlanSubscription,
  SessionService,
} from '../../services/session.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import dayjs from 'dayjs';
import { ServerConfigService } from '../../services/server-config.service';
import { GqlVertical } from '../../../generated/graphql';
import { AppConfigService } from '../../services/app-config.service';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../services/product.service';
import { Product } from '../../graphql/types';

@Component({
  selector: 'app-finalize-profile-modal',
  templateUrl: './finalize-profile-modal.component.html',
  styleUrls: ['./finalize-profile-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinalizeProfileModalComponent implements OnInit {
  loading = false;
  name: string;

  protected formFg = new FormGroup({
    email: createEmailFormControl<string>(''),
    plan: new FormControl<string>(undefined),
    terms: new FormControl<boolean>(false, [
      Validators.requiredTrue,
      Validators.required,
    ]),
  });
  protected canSkip = true;
  protected requiresPlan: boolean;
  protected product: Product;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly sessionService: SessionService,
    private readonly serverConfigService: ServerConfigService,
    private readonly appConfigService: AppConfigService,
    private readonly productService: ProductService,
  ) {}

  async ngOnInit() {
    this.sessionService.getSession().subscribe(async (session) => {
      const ageInDays = dayjs(new Date().getTime()).diff(
        session.user.createdAt,
        'days',
      );
      this.canSkip = ageInDays < 7;
      this.requiresPlan = needsPlanSubscription(
        session.user,
        this.serverConfigService,
      );
      if (this.requiresPlan) {
        this.formFg.controls.plan.addValidators(Validators.required);
        const vertical = await firstValueFrom(
          this.appConfigService.getActiveProductConfigChange(),
        );
        const cloudProducts = (
          await this.productService.listProducts({
            category: vertical.product,
          })
        ).filter((product) => product.isCloud);
        this.product = cloudProducts.find((product) =>
          product.prices.some((price) => price.price === 0),
        );
      }
      this.formFg.patchValue({
        email: session.user.email,
        terms: session.user.hasAcceptedTerms,
      });
      this.changeRef.detectChanges();
    });
  }

  async accept() {
    if (this.formFg.valid) {
      this.loading = true;
      this.changeRef.detectChanges();

      await this.sessionService.finalizeSignUp(
        this.formFg.value.email,
        this.product,
      );
      await new Promise((resolve) => setTimeout(resolve, 500));
      await this.modalCtrl.dismiss();
    }
    this.formFg.markAllAsTouched();
    this.changeRef.detectChanges();
  }
}
