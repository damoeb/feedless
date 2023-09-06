import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LinkCliPage } from './link-cli.page';
import { LinkCliPageModule } from './link-cli.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('LinkCliPage', () => {
  let component: LinkCliPage;
  let fixture: ComponentFixture<LinkCliPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        LinkCliPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LinkCliPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
