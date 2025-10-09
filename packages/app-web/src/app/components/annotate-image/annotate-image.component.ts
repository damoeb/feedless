import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  inject,
  input,
  OnDestroy,
  OnInit,
  viewChild,
} from '@angular/core';
import {
  FieldWrapper,
  GqlBoundingBoxInput,
  GqlXyPosition,
  Scalars,
} from '../../../generated/graphql';
import { debounce, DebouncedFunc } from 'lodash-es';
import { SourceBuilder } from '../interactive-website/source-builder';
import { firstValueFrom, Subscription } from 'rxjs';
import { Nullable } from '../../types';
import { NgClass } from '@angular/common';

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
  templateUrl: './annotate-image.component.html',
  styleUrls: ['./annotate-image.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgClass],
  standalone: true,
})
export class AnnotateImageComponent implements AfterViewInit, OnDestroy, OnInit {
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly embed = input.required<Embeddable>();

  readonly strokeStyle = input<string>('red');

  readonly sourceBuilder = input.required<SourceBuilder>();

  private pickedBoundingBox: EventEmitter<BoundingBox | null> =
    new EventEmitter<BoundingBox | null>();

  private pickedPosition: EventEmitter<XyPosition | null> = new EventEmitter<XyPosition | null>();

  readonly svgContainer = viewChild.required<ElementRef>('svgContainer');

  drag: boolean = false;
  mode: OperatorMode = 'move';
  box: Nullable<Box> = { x: 200, y: 200, w: 200, h: 100 };
  position: Nullable<{ x: number; y: number }> = { x: 100, y: 100 };

  private subscriptions: Subscription[] = [];

  private boxFrom: Nullable<{ x: number; y: number }>;
  private readonly drawBoxDebounced: DebouncedFunc<(box: Box) => void>;
  protected imageUrl: Nullable<string>;
  imageWidth: number = 0;
  imageHeight: number = 0;

  constructor() {
    this.drawBoxDebounced = debounce(this.drawBox, 5);
  }

  ngOnDestroy() {
    if (this.imageUrl) {
      URL.revokeObjectURL(this.imageUrl);
    }
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngAfterViewInit() {
    return this.drawImage();
  }

  handleMouseMove(event: MouseEvent) {
    const svgRect = this.svgContainer().nativeElement.getBoundingClientRect();
    const scaleX = this.imageWidth / svgRect.width;
    const scaleY = this.imageHeight / svgRect.height;

    const x = (event.clientX - svgRect.left) * scaleX;
    const y = (event.clientY - svgRect.top) * scaleY;

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
          const currentBox = {
            x: this.boxFrom.x,
            y: this.boxFrom.y,
            w: x - this.boxFrom.x,
            h: y - this.boxFrom.y,
          };
          this.drawBoxDebounced(this.normalizeBox(currentBox));
        }
        break;
    }
  }

  handleMouseDown(event: MouseEvent) {
    const svgRect = this.svgContainer().nativeElement.getBoundingClientRect();
    const scaleX = this.imageWidth / svgRect.width;
    const scaleY = this.imageHeight / svgRect.height;

    const x = (event.clientX - svgRect.left) * scaleX;
    const y = (event.clientY - svgRect.top) * scaleY;

    switch (this.mode) {
      case 'move':
        this.drag = true;
        break;
      case 'mark':
        this.boxFrom = { x, y };
        break;
    }
  }

  handleMouseUp(event: MouseEvent) {
    const svgRect = this.svgContainer().nativeElement.getBoundingClientRect();
    const scaleX = this.imageWidth / svgRect.width;
    const scaleY = this.imageHeight / svgRect.height;

    const x = (event.clientX - svgRect.left) * scaleX;
    const y = (event.clientY - svgRect.top) * scaleY;

    switch (this.mode) {
      case 'move':
        this.drag = false;
        break;
      case 'mark':
        if (this.boxFrom) {
          const currentBox = {
            x: this.boxFrom.x,
            y: this.boxFrom.y,
            w: x - this.boxFrom.x,
            h: y - this.boxFrom.y,
          };
          const box = this.normalizeBox(currentBox);
          if (box.h > 10 && box.w > 10) {
            this.box = box;
            this.pickedBoundingBox.emit(box);
          } else {
            this.box = null;
          }
        }
        this.boxFrom = null;
        break;
      case 'position':
        this.position = { x, y };
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
    this.imageUrl = URL.createObjectURL(this.b64toBlob(this.embed().data, this.embed().mimeType));
    image.src = this.imageUrl;

    image.onload = () => {
      this.imageWidth = image.width;
      this.imageHeight = image.height;

      // Set SVG dimensions
      const svgElement = this.svgContainer().nativeElement;
      svgElement.setAttribute('width', image.width);
      svgElement.setAttribute('height', image.height);
      svgElement.setAttribute('viewBox', `0 0 ${image.width} ${image.height}`);
    };

    this.changeRef.detectChanges();
  }

  private b64toBlob(b64Data: string, contentType: string, sliceSize: number = 512) {
    const byteCharacters = atob(b64Data);
    const byteArrays: BlobPart[] = [];

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
    // With SVG, we don't need to manually draw - the template handles rendering
    // The box property is bound to the template, so changes are automatic
    this.changeRef.detectChanges();
  }

  private normalizeBox(box: Box): Box {
    const { x: x1, y: y1, w: w1, h: h1 } = box;
    let x, y, w, h: number;

    if (w1 < 0) {
      x = x1 + w1;
      w = Math.abs(w1);
    } else {
      x = x1;
      w = w1;
    }

    if (h1 < 0) {
      y = y1 + h1;
      h = Math.abs(h1);
    } else {
      y = y1;
      h = h1;
    }

    return { x, y, w, h };
  }

  private drawPosition() {
    // With SVG, we don't need to manually draw - the template handles rendering
    // The position property is bound to the template, so changes are automatic
    this.changeRef.detectChanges();
  }

  deleteBox() {
    // todo impl
    // this.box = null;
    // this.pickedBoundingBox.emit(null);
    this.changeRef.detectChanges();
  }

  deletePosition() {
    // todo impl
    // this.position = null;
    // this.pickedPosition.emit(null);
    this.changeRef.detectChanges();
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.sourceBuilder().events.pickPoint.subscribe(
        (callback: (position: Nullable<XyPosition>) => void) => {
          console.log('pickPoint');
          this.mode = 'position';
          this.changeRef.detectChanges();
          firstValueFrom(this.pickedPosition).then((point) => {
            console.log('pickPoint', point);
            callback({
              x: this.toInt(point.x),
              y: this.toInt(point.y),
            });
            this.mode = 'move';
            this.changeRef.detectChanges();
          });
        }
      ),
      this.sourceBuilder().events.pickArea.subscribe(
        (callback: (box: Nullable<BoundingBox>) => void) => {
          console.log('pickArea');
          this.mode = 'mark';
          this.changeRef.detectChanges();
          firstValueFrom(this.pickedBoundingBox).then((bbox) => {
            console.log('pickArea', bbox);
            this.mode = 'move';
            callback({
              x: this.toInt(bbox.x),
              y: this.toInt(bbox.y),
              w: this.toInt(bbox.w),
              h: this.toInt(bbox.h),
            });
            this.changeRef.detectChanges();
          });
        }
      )
    );
  }

  private toInt(val: number) {
    return parseInt(val.toFixed(0));
  }
}
