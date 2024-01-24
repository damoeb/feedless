import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { GqlBoundingBoxInput, GqlXyPosition } from '../../../generated/graphql';
import { debounce, DebouncedFunc } from 'lodash-es';

export type XyPosition = GqlXyPosition;

interface Viewport {
  width: number;
  height: number;
}

export interface Embeddable {
  mimeType: string;
  data: string;
  url: string;
  viewport?: Viewport;
}

export type BoundingBox = GqlBoundingBoxInput;

type OperatorMode = 'move' | 'mark' | 'position';

interface Box {
  x: number;
  y: number;
  w: number;
  h: number;
}

@Component({
  selector: 'app-embedded-image',
  templateUrl: './embedded-image.component.html',
  styleUrls: ['./embedded-image.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmbeddedImageComponent
  implements AfterViewInit, OnDestroy, OnChanges {
  @Input({ required: true })
  embed: Embeddable;

  @Input()
  strokeStyle: string = 'red';

  @Input()
  pickBoundingBox: boolean = false;

  @Input()
  pickPosition: boolean = false;

  @Output()
  pickedBoundingBox: EventEmitter<BoundingBox | null> =
    new EventEmitter<BoundingBox | null>();

  @Output()
  pickedPosition: EventEmitter<XyPosition | null> =
    new EventEmitter<XyPosition | null>();

  @ViewChild('imageLayer', { static: false })
  imageLayerCanvas: ElementRef;

  @ViewChild('overlay', { static: false })
  overlayCanvas: ElementRef;

  @ViewChild('wrapper', { static: false })
  wrapper: ElementRef;

  drag: boolean = false;
  mode: OperatorMode = 'move';
  box: Box;
  position: { x: number; y: number };

  private boxFrom: { x: number; y: number };
  private readonly drawBoxDebounced: DebouncedFunc<(box: Box) => void>;
  private imageUrl: string;

  constructor(private readonly changeRef: ChangeDetectorRef) {
    this.drawBoxDebounced = debounce(this.drawBox, 5);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes.pickPosition?.currentValue ||
      changes.pickBoundingBox?.currentValue
    ) {
      if (changes.pickPosition?.currentValue) {
        this.mode = 'position';
      }
      if (changes.pickBoundingBox?.currentValue) {
        this.mode = 'mark';
      }
    } else {
      this.mode = 'move';
    }
    this.changeRef.detectChanges();
  }

  ngOnDestroy() {
    if (this.imageUrl) {
      URL.revokeObjectURL(this.imageUrl);
    }
  }

  async ngAfterViewInit() {
    return this.drawImage();
  }

  handleMouseMove(event: MouseEvent) {
    switch (this.mode) {
      case 'move':
        if (this.drag) {
          // todo mag set scrollx/y
          // this.x += event.movementX;
          // this.y += event.movementY;
        }
        break;
      case 'mark':
        if (this.boxFrom) {
          this.drawBoxDebounced(this.toBox(event));
        }
        break;
    }
  }

  handleMouseDown(event: MouseEvent) {
    switch (this.mode) {
      case 'move':
        this.drag = true;
        break;
      case 'mark':
        this.boxFrom = { x: event.offsetX, y: event.offsetY };
        break;
    }
  }

  handleMouseUp(event: MouseEvent) {
    switch (this.mode) {
      case 'move':
        this.drag = false;
        break;
      case 'mark':
        const box = this.toBox(event);
        if (box.h > 10 && box.w > 10) {
          this.box = box;
          this.pickedBoundingBox.emit(box);
        } else {
          this.box = null;
          this.drawBox({ x: 0, y: 0, w: 0, h: 0 });
        }
        this.boxFrom = null;
        break;
      case 'position':
        this.position = { x: event.offsetX, y: event.offsetY };
        this.drawPosition();
        this.pickedPosition.emit(this.position);
        break;
    }
  }

  handleMouseOut() {
    this.drag = false;
  }

  private revokeImageUrl() {
    if (this.imageUrl) {
      URL.revokeObjectURL(this.imageUrl);
    }
  }

  private async drawImage() {
    const image = new Image();
    this.revokeImageUrl();
    this.imageUrl = URL.createObjectURL(
      this.b64toBlob(this.embed.data, this.embed.mimeType)
    );
    image.src = this.imageUrl;

    image.onload = () => {
      this.imageLayerCanvas.nativeElement.height = image.height;
      this.imageLayerCanvas.nativeElement.width = image.width;
      this.overlayCanvas.nativeElement.height = image.height;
      this.overlayCanvas.nativeElement.width = image.width;
      const ctx = this.imageLayerCanvas.nativeElement.getContext('2d');
      ctx.drawImage(image, 0, 0);
    };

    this.changeRef.detectChanges();
  }

  private b64toBlob(
    b64Data: string,
    contentType: string,
    sliceSize: number = 512
  ) {
    const byteCharacters = atob(b64Data);
    const byteArrays: Uint8Array[] = [];

    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset + sliceSize);

      const byteNumbers = new Array(slice.length);
      for (let i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }

      byteArrays.push(new Uint8Array(byteNumbers));
    }

    return new Blob(byteArrays, { type: contentType });
  }

  private drawBox({ x, y, w, h }: Box) {
    console.log('drawBox');
    const { height: canvasHeight, width: canvasWidth } =
      this.overlayCanvas.nativeElement;
    const ctx: CanvasRenderingContext2D =
      this.overlayCanvas.nativeElement.getContext('2d');
    this.resetCanvas();
    if (w > 0 && h > 0) {
      ctx.strokeStyle = 'black';
      ctx.fillStyle = 'black';
      ctx.fillRect(0, 0, x, canvasHeight);
      ctx.fillRect(0, 0, canvasWidth, y);
      ctx.fillRect(0, y + h, canvasWidth, canvasHeight);
      ctx.fillRect(x + w, y, canvasWidth, canvasHeight);

      ctx.beginPath();
      ctx.rect(x, y, w, h);
      ctx.stroke();
    }
  }

  private toBox({ offsetX: x2, offsetY: y2 }: MouseEvent): Box {
    const { x: x1, y: y1 } = this.boxFrom;
    let x,
      y: number = 0;
    if (x1 < x2) {
      x = x1;
    } else {
      x = x2;
    }
    if (y1 < y2) {
      y = y1;
    } else {
      y = y2;
    }
    const w = Math.abs(x1 - x2);
    const h = Math.abs(y1 - y2);

    return { x, y, w, h };
  }

  private drawPosition() {
    this.resetCanvas();
    const ctx: CanvasRenderingContext2D =
      this.overlayCanvas.nativeElement.getContext('2d');
    ctx.strokeStyle = this.strokeStyle;
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.arc(this.position.x, this.position.y, 20, 0, 2 * Math.PI);
    ctx.stroke();
  }

  private resetCanvas() {
    const { height, width } = this.overlayCanvas.nativeElement;
    const ctx: CanvasRenderingContext2D =
      this.overlayCanvas.nativeElement.getContext('2d');
    ctx.clearRect(0, 0, height, width);
  }
}
