import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-empty-repository',
  templateUrl: './empty-repository.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./empty-repository.component.scss'],
})
export class EmptyRepositoryComponent {
  constructor() {}
}
