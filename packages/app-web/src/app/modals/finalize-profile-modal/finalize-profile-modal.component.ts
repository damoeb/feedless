import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { SessionService } from '../../services/session.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import dayjs from 'dayjs';

@Component({
  selector: 'app-finalize-profile-modal',
  templateUrl: './finalize-profile-modal.component.html',
  styleUrls: ['./finalize-profile-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinalizeProfileModalComponent implements OnInit {
  loading = false;
  name: string;

  protected formFg = new FormGroup({
    email: createEmailFormControl<string>(''),
    terms: new FormControl<boolean>(false, [Validators.requiredTrue, Validators.required]),
  });
  canSkip = true;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly sessionService: SessionService,
  ) {}

  ngOnInit() {
    this.sessionService.getSession().subscribe((session) => {
      const ageInDays = dayjs(new Date().getTime()).diff(
        session.user.createdAt,
        'days',
      );
      this.canSkip = ageInDays < 7;
      this.changeRef.detectChanges();
    });
  }

  async accept() {
    if (this.formFg.valid) {
      this.loading = true;
      this.changeRef.detectChanges();

      await this.sessionService.finalizeSignUp(this.formFg.value.email);
      await new Promise((resolve) => setTimeout(resolve, 500));
      await this.modalCtrl.dismiss();
    }
    this.formFg.markAllAsTouched();
    this.changeRef.detectChanges();
  }
}
