import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Record, Repository } from '../../../graphql/types';
import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonContent,
  IonSpinner,
  ModalController,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-tracker-details-page',
  templateUrl: './tracker-details.page.html',
  styleUrls: ['./tracker-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonBreadcrumbs, IonBreadcrumb, RouterLink, IonSpinner],
  standalone: true,
})
export class TrackerDetailsPage implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly modalCtrl = inject(ModalController);

  busy = false;
  documents: Record[];
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  repository: Repository;

  feedUrl: string;

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.trackerId) {
        }
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    URL.revokeObjectURL(this.diffImageUrl);
  }

  dismissModal() {
    this.modalCtrl.dismiss();
  }
}
