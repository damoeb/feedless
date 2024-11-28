import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonContent } from '@ionic/angular/standalone';
import { NotebooksComponent } from '../../../components/notebooks/notebooks.component';

@Component({
  selector: 'app-about-untold-notes',
  templateUrl: './about-untold-notes.page.html',
  styleUrls: ['./about-untold-notes.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, NotebooksComponent],
  standalone: true,
})
export class AboutUntoldNotesPage {}
