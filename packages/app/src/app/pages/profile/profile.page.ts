import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
})
export class ProfilePage implements OnInit {
  email: string;
  pending = false;

  constructor(private readonly authService: AuthService) { }

  ngOnInit() {
  }

  initiateSession() {
    this.pending = true;
    this.authService.requestAuthForUser(this.email)
      .subscribe((response) => {
        console.log('ws', response.data.authViaMail.token);
      });
  }
}
