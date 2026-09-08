import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { CreateAuctionAlertPage } from './create-auction-alert.page';

describe('CreateAuctionAlertPage', () => {
  let component: CreateAuctionAlertPage;
  let fixture: ComponentFixture<CreateAuctionAlertPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateAuctionAlertPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateAuctionAlertPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
