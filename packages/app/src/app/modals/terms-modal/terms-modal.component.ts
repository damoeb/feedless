import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ProfileService } from '../../services/profile.service';

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
    private readonly profileService: ProfileService
  ) {}

  ngOnInit() {
    this.name = this.profileService.getName();
    this.changeRef.detectChanges();
  }

  dismiss() {
    return this.modalCtrl.dismiss();
  }

  async accept() {
    this.loading = true;
    this.changeRef.detectChanges();
    await this.profileService.acceptTermsAndConditions();
    await new Promise((resolve) => setTimeout(resolve, 4000));
    await this.modalCtrl.dismiss();
  }
}
