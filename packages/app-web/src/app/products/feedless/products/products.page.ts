import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../../services/app-config.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { addIcons } from 'ionicons';
import { arrowForwardOutline } from 'ionicons/icons';
import {
  IonContent,
  IonBreadcrumbs,
  IonBreadcrumb,
  IonToolbar,
  IonButton,
  IonIcon,
} from '@ionic/angular/standalone';
import { NgIf, NgFor } from '@angular/common';
import { ProductHeadlineComponent } from '../../../components/product-headline/product-headline.component';

@Component({
  selector: 'app-feedless-products-page',
  templateUrl: './products.page.html',
  styleUrls: ['./products.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  imports: [
    IonContent,
    NgIf,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    ProductHeadlineComponent,
    IonToolbar,
    NgFor,
    IonButton,
    IonIcon,
  ],
  standalone: true,
})
export class ProductsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  product: VerticalSpecWithRoutes;
  videoUrl: SafeResourceUrl;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly domSanitizer: DomSanitizer,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ arrowForwardOutline });
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        this.product = (await this.appConfigService.getAllAppConfigs()).find(
          (p) => p.id === params.productId,
        );
        if (this.product?.videoUrl) {
          this.videoUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(
            this.product.videoUrl.replace('watch?v=', 'embed/'),
          );
        }
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getImageUrl() {
    return `url("${this.product?.imageUrl}")`;
  }

  isReleased() {
    return parseInt(`${this.product.version[0]}`) > 0;
  }
}
