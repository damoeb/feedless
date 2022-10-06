import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { OutputThrottleComponent } from './output-throttle.component';
import { OutputThrottleModule } from './output-throttle.module';

describe('OutputThrottleComponent', () => {
  let component: OutputThrottleComponent;
  let fixture: ComponentFixture<OutputThrottleComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [OutputThrottleModule],
      }).compileComponents();

      fixture = TestBed.createComponent(OutputThrottleComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
