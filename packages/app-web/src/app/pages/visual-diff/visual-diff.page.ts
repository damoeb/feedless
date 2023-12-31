import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';

type View = 'screenshot' | 'markup';

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

type CompareBy = 'pixel' | 'text' | 'markup';

@Component({
  selector: 'app-visual-diff',
  templateUrl: './visual-diff.page.html',
  styleUrls: ['./visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;

  form = new FormGroup({
    selector: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(1),
    ]),
    compareBy: new FormControl<CompareBy>('pixel', [Validators.required]),
    frequency: new FormControl<string>('day', [Validators.required]),
    subject: new FormControl<string>('', [Validators.required]),
    email: new FormControl<string>('', [Validators.required, Validators.email]),
    // boundingBox: new FormGroup({
    //   leftTop: new FormControl<string>('')
    // })
  });

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
