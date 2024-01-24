import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SearchAddressModalComponent } from './search-address-modal.component';
import { SearchAddressModalModule } from './search-address-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('SearchAddressModalComponent', () => {
  let component: SearchAddressModalComponent;
  let fixture: ComponentFixture<SearchAddressModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SearchAddressModalModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchAddressModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
