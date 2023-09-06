import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ContactPage } from './contact.page';
import { ContactPageModule } from './contact.module';
import { AppTestModule } from '../../app-test.module';

describe('ContactPage', () => {
  let component: ContactPage;
  let fixture: ComponentFixture<ContactPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ContactPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ContactPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
