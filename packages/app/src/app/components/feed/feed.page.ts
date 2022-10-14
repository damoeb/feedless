import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActualBucket } from '../../services/bucket.service';
import { ActualPagination } from '../../services/pagination.service';

@Component({
  selector: 'app-bucket',
  templateUrl: './feed.page.html',
  styleUrls: ['./feed.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedPage implements OnInit {
  feed: ActualBucket;
  pagination: ActualPagination;
  id: string;
  name: string;
  constructor(private readonly activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.id = params.id
    })
  }
}
