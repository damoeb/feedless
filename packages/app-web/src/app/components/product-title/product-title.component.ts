import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-product-title',
  templateUrl: './product-title.component.html',
  styleUrls: ['./product-title.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  standalone: true,
})
export class ProductTitleComponent implements OnInit, OnDestroy {
  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
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
