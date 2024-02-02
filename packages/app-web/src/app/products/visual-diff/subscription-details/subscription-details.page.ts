import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import pixelmatch from 'pixelmatch';
import { WebDocumentService } from '../../../services/web-document.service';
import { SourceSubscription, WebDocument } from '../../../graphql/types';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { dateFormat, dateTimeFormat } from '../../../services/profile.service';

type ImageSize = {
  width: number;
  height: number;
};

@Component({
  selector: 'app-visual-diff-details',
  templateUrl: './subscription-details.page.html',
  styleUrls: ['./subscription-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionDetailsPage implements OnInit, OnDestroy {
  busy = false;
  documents: WebDocument[];
  safeDiffImageUrl: SafeResourceUrl;
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  subscription: SourceSubscription;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly domSanitizer: DomSanitizer,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly webDocumentService: WebDocumentService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.id) {
          this.fetch(params.id);
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    URL.revokeObjectURL(this.diffImageUrl);
  }

  private async fetch(id: string) {
    const page = 0;
    this.busy = true;
    this.changeRef.detectChanges();

    this.subscription =
      await this.sourceSubscriptionService.getSubscriptionById(id);
    this.documents = await this.webDocumentService.findAllByStreamId({
      cursor: {
        page,
        pageSize: 2,
      },
      where: {
        sourceSubscription: {
          where: {
            id,
          },
        },
      },
    });

    if (this.documents.length > 1) {
      this.diffImageUrl = await this.createImageDiff(
        this.documents[0].contentRaw,
        this.documents[1].contentRaw,
      );
      this.safeDiffImageUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(
        this.diffImageUrl,
      );
    }

    this.busy = false;
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
