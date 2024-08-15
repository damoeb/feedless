import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedModalComponent } from './remote-feed-modal.component';
import { RemoteFeedModalModule } from './remote-feed-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedModalComponent', () => {
  let component: RemoteFeedModalComponent;
  let fixture: ComponentFixture<RemoteFeedModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RemoteFeedModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedModalComponent);
    component = fixture.componentInstance;
    component.feedProvider = jasmine.createSpy();
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
