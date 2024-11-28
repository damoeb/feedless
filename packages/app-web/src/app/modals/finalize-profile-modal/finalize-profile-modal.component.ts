import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import {
  ModalController,
  IonContent,
  IonList,
  IonRow,
  IonCol,
  IonLabel,
  IonInput,
  IonNote,
  IonCheckbox,
  IonToolbar,
  IonButtons,
  IonButton,
  IonSpinner,
} from '@ionic/angular/standalone';
import {
  needsPlanSubscription,
  SessionService,
} from '../../services/session.service';
import {
  FormControl,
  FormGroup,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import dayjs from 'dayjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AppConfigService } from '../../services/app-config.service';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../services/product.service';
import { Product } from '../../graphql/types';


@Component({
  selector: 'app-finalize-profile-modal',
  templateUrl: './finalize-profile-modal.component.html',
  styleUrls: ['./finalize-profile-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonList,
    IonRow,
    IonCol,
    IonLabel,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonNote,
    IonCheckbox,
    IonToolbar,
    IonButtons,
    IonButton,
    IonSpinner
],
  standalone: true,
})
export class FinalizeProfileModalComponent implements OnInit {
  private readonly modalCtrl = inject(ModalController);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly sessionService = inject(SessionService);
  private readonly serverConfigService = inject(ServerConfigService);
  private readonly appConfigService = inject(AppConfigService);
  private readonly productService = inject(ProductService);

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
