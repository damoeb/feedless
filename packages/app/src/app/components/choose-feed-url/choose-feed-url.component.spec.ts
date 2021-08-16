import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ChooseFeedUrlComponent } from './choose-feed-url.component';

describe('AddFeedUrlComponent', () => {
  let component: ChooseFeedUrlComponent;
  let fixture: ComponentFixture<ChooseFeedUrlComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ChooseFeedUrlComponent],
        imports: [IonicModule.forRoot()],
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
