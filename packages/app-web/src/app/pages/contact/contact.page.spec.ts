import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ContactPage } from './contact.page';
import { AppTestModule } from '../../app-test.module';

describe('ContactPage', () => {
  let component: ContactPage;
  let fixture: ComponentFixture<ContactPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ContactPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
