import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-external-link',
  templateUrl: './external-link.component.html',
  styleUrls: ['./external-link.component.scss'],
})
export class ExternalLinkComponent implements OnInit {
  @Input()
  title: string;

  @Input()
  url: string;

  @Input()
  fill = 'outline';

  constructor() {}

  ngOnInit() {}
}
