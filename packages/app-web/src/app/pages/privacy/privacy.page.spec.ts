import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PrivacyPage } from './privacy.page';
import { AppTestModule } from '../../app-test.module';

describe('PrivacyPage', () => {
  let component: PrivacyPage;
  let fixture: ComponentFixture<PrivacyPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrivacyPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(PrivacyPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
