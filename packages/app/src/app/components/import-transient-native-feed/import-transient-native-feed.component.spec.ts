import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ImportTransientNativeFeedComponent } from './import-transient-native-feed.component';

describe('ImportTransientNativeFeedComponent', () => {
  let component: ImportTransientNativeFeedComponent;
  let fixture: ComponentFixture<ImportTransientNativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ImportTransientNativeFeedComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportTransientNativeFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
