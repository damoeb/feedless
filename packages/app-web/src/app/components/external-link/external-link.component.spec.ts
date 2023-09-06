import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ExternalLinkComponent } from './external-link.component';
import { ExternalLinkModule } from './external-link.module';
import { AppTestModule } from '../../app-test.module';

describe('ExternalLinkComponent', () => {
  let component: ExternalLinkComponent;
  let fixture: ComponentFixture<ExternalLinkComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ExternalLinkModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ExternalLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
