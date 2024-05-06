import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositorySourcesPage } from './repository-sources.page';
import { RepositorySourcesPageModule } from './repository-sources.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule, mockRepository } from '../../../../app-test.module';

describe('RepositorySourcesPage', () => {
  let component: RepositorySourcesPage;
  let fixture: ComponentFixture<RepositorySourcesPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositorySourcesPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepository(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositorySourcesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
