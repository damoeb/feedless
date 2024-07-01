import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewChild,
} from '@angular/core';
import '@justinribeiro/lite-youtube';
import {
  AppConfigService,
  ProductConfig,
} from '../../../services/app-config.service';
import { IonPopover } from '@ionic/angular';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutFeedlessPage implements OnInit {
  @ViewChild('createOptions')
  createOptionsPopover: IonPopover;

  listedProducts: ProductConfig[];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly appConfigService: AppConfigService,
  ) {}

  async ngOnInit() {
    const allProducts = await this.appConfigService.getProductConfigs();
    this.listedProducts = allProducts.filter((p) => p.listed);
    // this.unstableProducts = allProducts.filter((p) => !p.listed);
    this.changeRef.detectChanges();
  }
}
