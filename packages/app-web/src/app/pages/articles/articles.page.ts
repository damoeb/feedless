import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-articles-page',
  templateUrl: './articles.page.html',
  styleUrls: ['./articles.page.scss'],
})
export class ArticlesPage implements OnInit {
  constructor() {}

  ngOnInit(): void {
    console.log('articles');
  }
}
