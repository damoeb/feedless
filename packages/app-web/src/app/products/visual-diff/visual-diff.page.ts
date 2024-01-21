import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { Authentication, AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ProductConfig, ProductService } from '../../services/product.service';

@Component({
  selector: 'app-visual-diff',
  templateUrl: './visual-diff.page.html',
  styleUrls: ['./visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  authorization: Authentication;
  config: ProductConfig;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly productService: ProductService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit() {
    this.config = this.productService.getProductConfig();
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
