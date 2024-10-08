import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedPreviewComponent } from './remote-feed-preview.component';
import { RemoteFeedPreviewModule } from './remote-feed-preview.module';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedPreviewComponent', () => {
  let component: RemoteFeedPreviewComponent;
  let fixture: ComponentFixture<RemoteFeedPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), RemoteFeedPreviewModule],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
