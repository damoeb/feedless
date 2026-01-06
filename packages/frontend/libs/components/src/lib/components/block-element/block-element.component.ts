import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-block-element',
  templateUrl: './block-element.component.html',
  styleUrls: ['./block-element.component.scss'],
  imports: [NgClass],
  standalone: true,
})
export class BlockElementComponent {
  readonly blocking = input<boolean>(true);
}
