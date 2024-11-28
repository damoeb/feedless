import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingFooterComponent } from './upcoming-footer.component';
import { AppTestModule } from '../../../app-test.module';

describe('PathHierarchyComponent', () => {
  let component: UpcomingFooterComponent;
  let fixture: ComponentFixture<UpcomingFooterComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [UpcomingFooterComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(UpcomingFooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
