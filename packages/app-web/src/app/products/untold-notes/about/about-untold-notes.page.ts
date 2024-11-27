import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-about-untold-notes',
    templateUrl: './about-untold-notes.page.html',
    styleUrls: ['./about-untold-notes.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class AboutUntoldNotesPage {}
