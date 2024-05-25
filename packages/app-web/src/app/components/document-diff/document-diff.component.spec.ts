import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DocumentDiffComponent } from './document-diff.component';
import { AppTestModule } from '../../../app-test.module';
import { DocumentDiffModule } from './document-diff.module';

describe('SubscriptionDetailsPage', () => {
  let component: DocumentDiffComponent;
  let fixture: ComponentFixture<DocumentDiffComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [DocumentDiffModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentDiffComponent);
    component = fixture.componentInstance;
    component.repository = {} as any;
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
