import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import pixelmatch from 'pixelmatch';
import { Record } from '../../graphql/types';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { dateFormat, dateTimeFormat } from '../../services/session.service';
import { addIcons } from 'ionicons';
import { arrowForwardOutline } from 'ionicons/icons';
import { NgIf, DatePipe } from '@angular/common';
import { IonIcon } from '@ionic/angular/standalone';

type ImageSize = {
  width: number;
  height: number;
};

@Component({
  selector: 'app-image-diff',
  templateUrl: './image-diff.component.html',
  styleUrls: ['./image-diff.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [NgIf, IonIcon, DatePipe],
  standalone: true,
})
export class ImageDiffComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  before: Record;

  @Input()
  after: Record;

  safeDiffImageUrl: SafeResourceUrl;
  private diffImageUrl: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly domSanitizer: DomSanitizer,
  ) {
    addIcons({ arrowForwardOutline });
  }

  ngOnDestroy(): void {
    URL.revokeObjectURL(this.diffImageUrl);
  }

  async ngOnInit() {
    if (this.after) {
      this.diffImageUrl = await this.createImageDiff(
        this.before.rawBase64,
        this.after.rawBase64,
      );
      this.safeDiffImageUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(
        this.diffImageUrl,
      );
    }

    this.changeRef.detectChanges();
  }

  private createImage(objectURL: string) {
    return new Promise<HTMLImageElement>((resolve, reject) => {
      const image = new Image();
      image.src = 'data:image/png;base64, ' + objectURL;
      image.setAttribute('crossOrigin', 'anonymous');
      image.addEventListener('load', () => resolve(image));
      image.addEventListener('error', (error) => reject(error));
    });
  }

  private async createCanvasContext(objectURL: string, canvasSize?: ImageSize) {
    const img = await this.createImage(objectURL);
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error('Failed to create a CanvasRenderingContext.');
    }
    const size: ImageSize = {
      width: img.naturalWidth,
      height: img.naturalHeight,
    };
    canvas.width = canvasSize ? canvasSize.width : size.width;
    canvas.height = canvasSize ? canvasSize.height : size.height;
    context.drawImage(img, 0, 0);
    return { context, size };
  }

  private async getBlobByCanvas(canvas: HTMLCanvasElement) {
    return new Promise<Blob>((resolve, reject) => {
      canvas.toBlob((blob) => {
        if (blob) {
          resolve(blob);
        } else {
          reject();
        }
      }, 'image/png');
    });
  }

  private async createImageDiff(
    img1Base64: string,
    img2Base64: string,
  ): Promise<string> {
    const image1 = await this.createCanvasContext(img1Base64);
    // Fits the size of image1
    const diffSize = image1.size;
    const image2 = await this.createCanvasContext(img2Base64, diffSize);

    const diffCanvas = document.createElement('canvas');
    const diffContext = diffCanvas.getContext('2d');
    if (!diffContext) {
      throw new Error('Failed to create a diff CanvasRenderingContext.');
    }
    diffCanvas.width = diffSize.width;
    diffCanvas.height = diffSize.height;
    const outputDiff = diffContext.createImageData(
      diffSize.width,
      diffSize.height,
    );

    const numDiffPixels = pixelmatch(
      image1.context.getImageData(0, 0, diffSize.width, diffSize.height).data,
      image2.context.getImageData(0, 0, diffSize.width, diffSize.height).data,
      outputDiff.data,
      diffSize.width,
      diffSize.height,
      {
        threshold: 0,
        diffColor: [255, 0, 0],
      },
    );

    console.log({
      numDiffPixels,
      width: diffSize.width,
      height: diffSize.height,
      diffPercentage:
        (100 * numDiffPixels) / (diffSize.width * diffSize.height),
    });

    diffContext.putImageData(outputDiff, 0, 0);

    return URL.createObjectURL(await this.getBlobByCanvas(diffCanvas));
  }

  protected readonly dateFormat = dateFormat;
  protected readonly dateTimeFormat = dateTimeFormat;
}
