import { Component } from '@angular/core';
import { BucketService } from './services/bucket.service';
import { GqlBucket, GqlNotebook } from '../generated/graphql';
import { ToastService } from './services/toast.service';
import { ModalController } from '@ionic/angular';
import { BucketCreateComponent } from './components/bucket-create/bucket-create.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {}
