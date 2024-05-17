import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReaderMenuComponent } from './reader-menu.component';
import { ReaderMenuModule } from './reader-menu.module';
import { AppTestModule } from '../../../app-test.module';

describe('ReaderMenuComponent', () => {
  let component: ReaderMenuComponent;
  let fixture: ComponentFixture<ReaderMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ReaderMenuModule, AppTestModule.withDefaults()],
      providers: [
        // { provide: ApolloMockController, useValue: AgentService }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
