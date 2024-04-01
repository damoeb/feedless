import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductConfig, ProductService } from '../../../services/product.service';
import { ModalController } from '@ionic/angular';
import { FormControl, FormGroup } from '@angular/forms';

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
  })

  freeFG = new FormGroup({
    name: new FormControl(),
    message: new FormControl(),
  })

  offerFG = new FormGroup({
    name: new FormControl(),
    email: new FormControl(),
    organization: new FormControl(),
    price: new FormControl(),
    message: new FormControl(),
  })

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        this.product = (await this.productService.getProductConfigs()).find(
          (p) => p.id === params.productId,
        );
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

  sendFreeLicenseRequest() {

  }

  sendOfferRequest() {

  }

  buyIndividualLicense() {

  }
}
