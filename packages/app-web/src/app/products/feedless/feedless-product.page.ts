import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonRouterOutlet,
  IonToolbar,
} from '@ionic/angular/standalone';
import { SessionService } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-feedless-product-page',
  templateUrl: './feedless-product.page.html',
  styleUrls: ['./feedless-product.page.scss'],
  imports: [
    IonContent,
    IonRouterOutlet,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    RouterLinkActive,
    RouterLink,
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedlessProductPage implements OnInit {
  private readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly serverConfigService = inject(ServerConfigService);
  needsAcceptTerms: boolean = false;

  ngOnInit(): void {
    this.sessionService.getSession().subscribe((session) => {
      this.needsAcceptTerms =
        session?.isLoggedIn && !session?.user.hasAcceptedTerms;
      this.changeRef.detectChanges();
    });
  }

  async acceptsTerms() {
    await this.sessionService.finalizeSignUp();
  }
}
