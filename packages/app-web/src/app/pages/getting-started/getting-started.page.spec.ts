import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GettingStartedPage } from './getting-started.page';
import { GettingStartedPageModule } from './getting-started.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('GettingStartedPage', () => {
  let component: GettingStartedPage;
  let fixture: ComponentFixture<GettingStartedPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        GettingStartedPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GettingStartedPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
