import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { PreviewTransientGenericFeedComponent } from './preview-transient-generic-feed.component';

describe('PreviewTransientGenericFeedComponent', () => {
  let component: PreviewTransientGenericFeedComponent;
  let fixture: ComponentFixture<PreviewTransientGenericFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewTransientGenericFeedComponent ],
      imports: [IonicModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(PreviewTransientGenericFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
