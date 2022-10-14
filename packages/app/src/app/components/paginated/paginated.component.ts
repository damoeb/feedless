import { Component, Input, OnInit } from '@angular/core';
import { ActualPagination } from '../../services/pagination.service';

@Component({
  selector: 'app-paginated',
  templateUrl: './paginated.component.html',
  styleUrls: ['./paginated.component.scss'],
})
export class PaginatedComponent implements OnInit {

  @Input()
  pagination: ActualPagination

  constructor() { }

  ngOnInit() {}

}
