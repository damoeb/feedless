import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmbeddedWebsiteComponent } from './embedded-website.component';
import { EmbeddedWebsiteModule } from './embedded-website.module';
import { AppTestModule } from '../../app-test.module';

describe('EmbededWebsiteComponent', () => {
  let component: EmbeddedWebsiteComponent;
  let fixture: ComponentFixture<EmbeddedWebsiteComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EmbeddedWebsiteModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(EmbeddedWebsiteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
