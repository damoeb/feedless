import { Component, OnInit } from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import { Profile, ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
})
export class ProfilePage implements OnInit {
  profile: Profile;

  constructor(
    private readonly opmlService: OpmlService,
    private readonly router: Router,
    private readonly profileService: ProfileService
  ) {}

  async importOpml(uploadEvent: Event) {
    await this.opmlService.convertOpmlToJson(uploadEvent);
  }

  async exportOpml() {}

  ngOnInit(): void {
    this.profile = this.profileService.getProfile();
  }

  async logout() {
    await this.profileService.logout();
    await this.router.navigateByUrl('/');
  }
}
