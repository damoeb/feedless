import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ImportTransientGenericFeedComponent } from './import-transient-generic-feed.component';

describe('PreviewTransientGenericFeedComponent', () => {
  let component: ImportTransientGenericFeedComponent;
  let fixture: ComponentFixture<ImportTransientGenericFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportTransientGenericFeedComponent ],
      imports: [IonicModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportTransientGenericFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
