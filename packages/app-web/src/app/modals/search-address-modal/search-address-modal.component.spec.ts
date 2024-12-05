import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchAddressModalComponent } from './search-address-modal.component';
import { AppTestModule } from '../../app-test.module';
import { SearchAddressModalModule } from './search-address-modal.module';

describe('SearchAddressModalComponent', () => {
  let component: SearchAddressModalComponent;
  let fixture: ComponentFixture<SearchAddressModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchAddressModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchAddressModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
