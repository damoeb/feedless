import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';

describe('FeedBuilderModalComponent', () => {
  let component: FeedBuilderModalComponent;
  let fixture: ComponentFixture<FeedBuilderModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedBuilderModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
