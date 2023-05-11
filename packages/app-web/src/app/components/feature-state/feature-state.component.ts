import { Component, Input, OnInit } from '@angular/core';
import { GqlFeatureState } from '../../../generated/graphql';

@Component({
  selector: 'app-feature-state',
  templateUrl: './feature-state.component.html',
  styleUrls: ['./feature-state.component.scss'],
})
export class FeatureStateComponent implements OnInit {
  @Input()
  state: GqlFeatureState;
  visible: boolean;

  constructor() {}

  ngOnInit() {
    this.visible = ![GqlFeatureState.Stable, GqlFeatureState.Off].includes(
      this.state
    );
  }
}
