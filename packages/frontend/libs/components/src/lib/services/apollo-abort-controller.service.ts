import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ApolloAbortControllerService extends AbortController {}
