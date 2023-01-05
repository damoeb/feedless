import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ImportExistingNativeFeedComponent } from './import-existing-native-feed.component';

describe('ImportTransientNativeFeedComponent', () => {
  let component: ImportExistingNativeFeedComponent;
  let fixture: ComponentFixture<ImportExistingNativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ImportExistingNativeFeedComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportExistingNativeFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
