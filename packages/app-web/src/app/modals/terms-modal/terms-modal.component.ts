import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-terms-modal',
  templateUrl: './terms-modal.component.html',
  styleUrls: ['./terms-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TermsModalComponent implements OnInit {
  loading = false;
  name: string;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly sessionService: SessionService,
  ) {}

  ngOnInit() {
    this.changeRef.detectChanges();
  }

  dismiss() {
    return this.modalCtrl.dismiss();
  }

  async accept() {
    this.loading = true;
    this.changeRef.detectChanges();
    await this.sessionService.acceptTermsAndConditions();
    await new Promise((resolve) => setTimeout(resolve, 500));
    await this.modalCtrl.dismiss();
  }
}
