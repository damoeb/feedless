import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderComponent } from './feed-builder.component';
import { AppTestModule } from '../../app-test.module';
import { FeedBuilderModule } from './feed-builder.module';

describe('FeedBuilderPage', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedBuilderModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
