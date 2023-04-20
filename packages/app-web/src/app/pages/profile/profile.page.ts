import { Component, OnInit } from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import {
  Profile,
  ProfileService,
  UserSecret,
} from '../../services/profile.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
})
export class ProfilePage implements OnInit {
  profile: Profile;
  secrets: UserSecret[] = [];

  constructor(
    private readonly opmlService: OpmlService,
    private readonly router: Router,
    private readonly profileService: ProfileService
  ) {}

  async importOpml(uploadEvent: Event) {
    await this.opmlService.convertOpmlToJson(uploadEvent);
  }

  async exportOpml() {
    // await this.opmlService.exportOpml();
  }

  ngOnInit(): void {
    this.profile = this.profileService.getProfile();
    this.secrets.push(...this.profile.user.secrets);
  }

  async logout() {
    await this.profileService.logout();
    await this.router.navigateByUrl('/');
  }

  async creteApiToken() {
    const apiToken = await this.profileService.createApiToken();
    console.log(apiToken);
    this.secrets.push(apiToken);
  }

  async deleteSecret(secret: UserSecret) {
    await this.profileService.deleteApiTokens({
      where: {
        in: [secret.id],
      },
    });
  }
}
