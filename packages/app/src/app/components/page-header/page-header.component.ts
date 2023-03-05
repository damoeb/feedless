import { Component, OnInit } from '@angular/core';
import { WizardComponent } from '../wizard/wizard/wizard.component';
import { ModalController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
})
export class PageHeaderComponent implements OnInit {
  authorization: Authentication;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  async openFeedWizard() {
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      showBackdrop: false,
    });
    await modal.present();
  }

  ngOnInit(): void {
    this.authService.authorizationChange().subscribe((authorization) => {
      this.authorization = authorization;
      console.log('authorization', authorization);
    });
  }

  async handleProfileAction(event: any) {
    switch (event.detail.value) {
      case 'profile':
        await this.router.navigateByUrl('/profile');
        break;
      case 'logout':
        await this.profileService.logout();
        break;
    }
  }
}
