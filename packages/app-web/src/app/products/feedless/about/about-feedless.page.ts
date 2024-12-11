import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import '@justinribeiro/lite-youtube';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../../services/app-config.service';
import {
  IonBadge,
  IonContent,
  IonIcon,
  IonItem,
  IonList,
  IonListHeader,
} from '@ionic/angular/standalone';
import { ModalName } from '../../../services/modal.service';
import { addIcons } from 'ionicons';
import {
  chevronForwardOutline,
  eyeOutline,
  gitNetworkOutline,
  listOutline,
} from 'ionicons/icons';
import { FeedlessHeaderComponent } from '../../../components/feedless-header/feedless-header.component';
import { ProductHeaderComponent } from '../../../components/product-header/product-header.component';
import { RouterLink } from '@angular/router';
import { RemoveIfProdDirective } from '../../../directives/remove-if-prod/remove-if-prod.directive';
import { PromotionHeaderComponent } from '../../../components/promotion-header/promotion-header.component';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FeedlessHeaderComponent,
    IonContent,
    ProductHeaderComponent,
    IonList,
    IonListHeader,
    IonItem,
    RouterLink,
    IonIcon,
    RemoveIfProdDirective,
    IonBadge,
    PromotionHeaderComponent,
  ],
  standalone: true,
})
export class AboutFeedlessPage implements OnInit {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly appConfigService = inject(AppConfigService);

  listedProducts: VerticalSpecWithRoutes[];

  constructor() {
    addIcons({
      listOutline,
      chevronForwardOutline,
      eyeOutline,
      gitNetworkOutline,
    });
  }

  async ngOnInit() {
    const allProducts = await this.appConfigService.getAllAppConfigs();
    this.listedProducts = allProducts.filter((p) => p.listed);
    // this.unstableProducts = allProducts.filter((p) => !p.listed);
    this.changeRef.detectChanges();
  }

  protected readonly ModalName = ModalName;
}
