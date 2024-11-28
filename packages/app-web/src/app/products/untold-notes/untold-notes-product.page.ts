import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { DarkModeButtonComponent } from '../../components/dark-mode-button/dark-mode-button.component';
import { LoginButtonComponent } from '../../components/login-button/login-button.component';

@Component({
  selector: 'app-untold-notes-product-page',
  templateUrl: './untold-notes-product.page.html',
  styleUrls: ['./untold-notes-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    RouterLink,
    DarkModeButtonComponent,
    LoginButtonComponent,
    IonContent,
    IonRouterOutlet,
  ],
  standalone: true,
})
export class UntoldNotesProductPage implements OnInit, OnDestroy {
  private readonly appConfigService = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  scrapeResponse: ScrapeResponse;
  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
