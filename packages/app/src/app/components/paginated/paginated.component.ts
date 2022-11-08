import { Component, Input, OnInit } from '@angular/core';
import { Pagination } from '../../services/pagination.service';

@Component({
  selector: 'app-paginated',
  templateUrl: './paginated.component.html',
  styleUrls: ['./paginated.component.scss'],
})
export class PaginatedComponent implements OnInit {

  @Input()
  pagination: Pagination

  constructor() { }

  ngOnInit() {}

}
