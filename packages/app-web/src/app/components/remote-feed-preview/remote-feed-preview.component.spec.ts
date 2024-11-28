import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedPreviewComponent } from './remote-feed-preview.component';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedPreviewComponent', () => {
  let component: RemoteFeedPreviewComponent;
  let fixture: ComponentFixture<RemoteFeedPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), RemoteFeedPreviewComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
