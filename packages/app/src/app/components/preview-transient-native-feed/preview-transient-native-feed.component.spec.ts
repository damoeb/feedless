import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { PreviewTransientNativeFeedComponent } from './preview-transient-native-feed.component';

describe('PreviewTransientNativeFeedComponent', () => {
  let component: PreviewTransientNativeFeedComponent;
  let fixture: ComponentFixture<PreviewTransientNativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewTransientNativeFeedComponent ],
      imports: [IonicModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(PreviewTransientNativeFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
