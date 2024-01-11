import { Component } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
})
export class AgentsPage {
  gridLayout = false;
  entities: [] =
    [];
  isLast = false;
  private currentPage = 0;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  createAgent() {}
}
