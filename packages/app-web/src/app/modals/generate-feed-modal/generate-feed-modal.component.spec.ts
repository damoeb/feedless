import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { GenerateFeedModalModule } from './generate-feed-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('GenerateFeedModalComponent', () => {
  let component: GenerateFeedModalComponent;
  let fixture: ComponentFixture<GenerateFeedModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), GenerateFeedModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(GenerateFeedModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
