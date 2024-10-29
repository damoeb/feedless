import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingHeaderComponent } from './upcoming-header.component';
import { AppTestModule } from '../../../app-test.module';
import { UpcomingHeaderModule } from './upcoming-header.module';

describe('UpcomingHeaderComponent', () => {
  let component: UpcomingHeaderComponent;
  let fixture: ComponentFixture<UpcomingHeaderComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [UpcomingHeaderModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(UpcomingHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
