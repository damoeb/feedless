import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeatureStateComponent } from './feature-state.component';
import { FeatureStateModule } from './feature-state.module';
import { AppTestModule } from '../../app-test.module';

describe('FeatureStateComponent', () => {
  let component: FeatureStateComponent;
  let fixture: ComponentFixture<FeatureStateComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeatureStateModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
