import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchAboButtonComponent } from './search-abo-button.component';
import { AppTestModule } from '@feedless/testing';

describe('SearchAboButtonComponent', () => {
  let component: SearchAboButtonComponent;
  let fixture: ComponentFixture<SearchAboButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchAboButtonComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchAboButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
