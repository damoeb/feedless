import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss'],
})
export class WelcomeComponent implements OnInit {
  statements = [
    'Generate a Feed from any Website',
    'Filter a Feed from any Website',
    'Convert a ATOM Feed to a JSON Feed',
    'Push Feed Items to Email',
    'Push Feed Items to Webhook',
    'Push Feed Items to Websocket',
  ];

  constructor() {}

  ngOnInit() {}
}
