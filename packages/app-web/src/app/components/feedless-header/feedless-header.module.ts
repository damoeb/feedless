import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessHeaderComponent } from './feedless-header.component';
import { ProductHeadlineModule } from '../product-headline/product-headline.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { AgentsButtonModule } from '../agents-button/agents-button.module';
import { DarkModeButtonModule } from '../dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../login-button/login-button.module';
import { RepositoriesButtonModule } from '../repositories-button/repositories-button.module';
import { RouterLink } from '@angular/router';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
import {
  IonHeader,
  IonToolbar,
  IonLabel,
  IonButton,
  IonButtons,
  IonMenuButton,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FeedlessHeaderComponent],
  exports: [FeedlessHeaderComponent],
  imports: [
    CommonModule,
    ProductHeadlineModule,
    SearchbarModule,
    AgentsButtonModule,
    DarkModeButtonModule,
    LoginButtonModule,
    RepositoriesButtonModule,
    RouterLink,
    RemoveIfProdModule,
    IonHeader,
    IonToolbar,
    IonLabel,
    IonButton,
    IonButtons,
    IonMenuButton,
  ],
})
export class FeedlessHeaderModule {}
