import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GeneratedFeedComponent } from './generated-feed.component';
import { GeneratedFeedModule } from './generated-feed.module';

describe('GeneratedFeedComponent', () => {
  let component: GeneratedFeedComponent;
  let fixture: ComponentFixture<GeneratedFeedComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [GeneratedFeedModule],
      }).compileComponents();

      fixture = TestBed.createComponent(GeneratedFeedComponent);
      component = fixture.componentInstance;
      component.feed = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
