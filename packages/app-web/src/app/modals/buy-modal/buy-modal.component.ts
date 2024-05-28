import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface ModalModalComponentProps {
  // tags: string[];
}

@Component({
  selector: 'app-buy-modal',
  templateUrl: './buy-modal.component.html',
  styleUrls: ['./buy-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BuyModalComponent implements ModalModalComponentProps, OnInit {
  protected loading = true;
  protected hasCheckoutLoaded = false;
  private timeoutId: any;

  constructor(private readonly modalCtrl: ModalController,
              private readonly changeRef: ChangeDetectorRef) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  handleIframeResponse(event: Event, success: boolean) {
    if (!success) {
      console.error(event)
    }
    this.hasCheckoutLoaded = success;
    this.loading = false;
    clearTimeout(this.timeoutId);
    this.changeRef.detectChanges();
  }

  ngOnInit(): void {
    this.timeoutId = setTimeout(() => {
      this.handleIframeResponse(new CustomEvent('timeout'), false)
    }, 8000)
  }
}
