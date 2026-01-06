import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedlessHeaderComponent } from './feedless-header.component';
import { AppTestModule } from '@feedless/test';

describe('FeedlessHeaderComponent', () => {
  let component: FeedlessHeaderComponent;
  let fixture: ComponentFixture<FeedlessHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedlessHeaderComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedlessHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
