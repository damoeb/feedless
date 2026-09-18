import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-block-element',
  templateUrl: './block-element.component.html',
  styleUrls: ['./block-element.component.scss'],
  imports: [NgClass],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class BlockElementComponent {
  readonly blocking = input<boolean>(true);
}
