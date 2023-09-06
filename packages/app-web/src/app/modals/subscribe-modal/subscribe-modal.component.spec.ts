import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SubscribeModalComponent } from './subscribe-modal.component';
import { SubscribeModalModule } from './subscribe-modal.module';

describe('SubscribeModalComponent', () => {
  let component: SubscribeModalComponent;
  let fixture: ComponentFixture<SubscribeModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SubscribeModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscribeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
