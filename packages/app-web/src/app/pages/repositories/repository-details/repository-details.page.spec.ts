import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryDetailsPage } from './repository-details.page';
import { RepositoryDetailsPageModule } from './repository-details.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule, mockRepository } from '../../../app-test.module';

describe('RepositoryDetailsPage', () => {
  let component: RepositoryDetailsPage;
  let fixture: ComponentFixture<RepositoryDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryDetailsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepository(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
