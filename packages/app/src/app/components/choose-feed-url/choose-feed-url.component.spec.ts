import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChooseFeedUrlComponent } from './choose-feed-url.component';
import { ChooseFeedUrlModule } from './choose-feed-url.module';

describe('AddFeedUrlComponent', () => {
  let component: ChooseFeedUrlComponent;
  let fixture: ComponentFixture<ChooseFeedUrlComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ChooseFeedUrlModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ChooseFeedUrlComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
