import { Injectable } from '@angular/core';

export type ActualPagination = { __typename?: 'Pagination'; totalPages: number; page: number; isLast: boolean; isFirst: boolean; isEmpty: boolean };

@Injectable({
  providedIn: 'root'
})
export class PaginationService {

  constructor() { }
}
