import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { fixUrl } from '../../../app.module';
import { IonContent } from '@ionic/angular/standalone';
import { ProductHeaderComponent } from '../../../components/product-header/product-header.component';
import { SearchbarComponent } from '../../../elements/searchbar/searchbar.component';

@Component({
  selector: 'app-about-visual-diff',
  templateUrl: './about-visual-diff.page.html',
  styleUrls: ['./about-visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, ProductHeaderComponent, SearchbarComponent],
  standalone: true,
})
export class AboutVisualDiffPage {
  private readonly router = inject(Router);

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/tracker'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }
}
