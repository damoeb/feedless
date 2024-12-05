import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedlessMenuComponent } from './feedless-menu.component';
import { AppTestModule } from '../../../app-test.module';

describe('FeedlessMenuComponent', () => {
  let component: FeedlessMenuComponent;
  let fixture: ComponentFixture<FeedlessMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedlessMenuComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedlessMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
