import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedlessMenuComponent } from './feedless-menu.component';
import { FeedlessMenuModule } from './feedless-menu.module';
import { AppTestModule } from '../../../app-test.module';

describe('FeedlessMenuComponent', () => {
  let component: FeedlessMenuComponent;
  let fixture: ComponentFixture<FeedlessMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedlessMenuModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedlessMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
