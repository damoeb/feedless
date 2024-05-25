import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductConfig, ProductService } from '../../services/product.service';
import { ModalController } from '@ionic/angular';
import { FormControl, FormGroup } from '@angular/forms';

type TargetGroup = 'organization' | 'individual' | 'other';
type ServiceFlavor = 'self' | 'cloud';

@Component({
  selector: 'app-buy-page',
  templateUrl: './buy.page.html',
  styleUrls: ['./buy.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BuyPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  product: ProductConfig;

  individualFG = new FormGroup({
    name: new FormControl(),
    email: new FormControl(),
  });

  freeFG = new FormGroup({
    name: new FormControl(),
    message: new FormControl(),
  });

  offerFG = new FormGroup({
    name: new FormControl(),
    email: new FormControl(),
    organization: new FormControl(),
    price: new FormControl(),
    message: new FormControl(),
  });
  targetGroup = new FormControl<TargetGroup>('individual');
  serviceFlavor = new FormControl<ServiceFlavor>('self');
  serviceFlavorSelf: ServiceFlavor = 'self';
  serviceFlavorCloud: ServiceFlavor = 'cloud';
  targetGroupOrganization: TargetGroup = 'organization';
  targetGroupIndividual: TargetGroup = 'individual';
  targetGroupOther: TargetGroup = 'other';

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    const productConfigs = await this.productService.getProductConfigs();
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        this.product = productConfigs.find((p) => p.id === params.productId);
        this.changeRef.detectChanges();
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  sendFreeLicenseRequest() {}

  sendOfferRequest() {}

  buyIndividualLicense() {}
}
