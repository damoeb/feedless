import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-puppeteer-evaluate-modal',
  templateUrl: './puppeteer-evaluate-modal.component.html',
  styleUrls: ['./puppeteer-evaluate-modal.component.scss'],
})
export class PuppeteerEvaluateModalComponent implements OnInit {
  @Input()
  evaluateScript: string;
  control: FormControl<string | null> = new FormControl<string>('');

  constructor(private readonly modalCtrl: ModalController) {}

  async closeModal(persist = false) {
    if (persist) {
      await this.modalCtrl.dismiss(this.control.value, 'persist');
    } else {
      await this.modalCtrl.dismiss();
    }
  }

  ngOnInit(): void {
    this.control.setValue(this.evaluateScript);
  }
}
