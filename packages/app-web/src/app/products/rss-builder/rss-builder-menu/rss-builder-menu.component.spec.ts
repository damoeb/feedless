import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RssBuilderMenuComponent } from './rss-builder-menu.component';
import { RssBuilderMenuModule } from './rss-builder-menu.module';
import { AppTestModule } from '../../../app-test.module';

describe('RssBuilderMenuComponent', () => {
  let component: RssBuilderMenuComponent;
  let fixture: ComponentFixture<RssBuilderMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RssBuilderMenuModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(RssBuilderMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
