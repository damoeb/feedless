import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { BoundingBox, XyPosition } from '../../components/embedded-image/embedded-image.component';
import { GqlXyPosition } from '../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';

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
  embedScreenshot: Embeddable;
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;

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

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      // this.highlightXpath = xpath;
      this.pickElementDelegate(xpath);
      this.pickElementDelegate = null;
    }
  }

  handlePickedPosition(position: XyPosition | null) {
    if (this.pickPositionDelegate) {
      this.pickPositionDelegate(position);
      this.pickPositionDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  handlePickedBoundingBox(boundingBox: BoundingBox | null) {
    if (this.pickBoundingBoxDelegate) {
      this.pickBoundingBoxDelegate(boundingBox);
      this.pickBoundingBoxDelegate = null;
      this.changeRef.detectChanges();
    }
  }
}
