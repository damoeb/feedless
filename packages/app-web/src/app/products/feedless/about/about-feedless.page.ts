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
  VerticalSpecWithRoutes,
} from '../../../services/app-config.service';
import { IonPopover } from '@ionic/angular/standalone';
import { ModalName } from '../../../services/modal.service';
import { addIcons } from 'ionicons';
import {
  chevronForwardOutline,
  eyeOutline,
  gitNetworkOutline,
  listOutline,
} from 'ionicons/icons';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutFeedlessPage implements OnInit {
  @ViewChild('createOptions')
  createOptionsPopover: IonPopover;

  listedProducts: VerticalSpecWithRoutes[];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly appConfigService: AppConfigService,
  ) {
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
