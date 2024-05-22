import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BuyModalComponent } from './buy-modal.component';
import { BuyModalModule } from './buy-modal.module';

describe('ExportModalComponent', () => {
  let component: BuyModalComponent;
  let fixture: ComponentFixture<BuyModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BuyModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BuyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
