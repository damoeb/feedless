import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';

describe('TransformWebsiteToFeedComponent', () => {
  let component: TransformWebsiteToFeedComponent;
  let fixture: ComponentFixture<TransformWebsiteToFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TransformWebsiteToFeedComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(TransformWebsiteToFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
