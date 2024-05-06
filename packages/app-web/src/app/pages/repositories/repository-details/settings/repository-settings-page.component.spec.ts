import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositorySettingsPage } from './repository-settings-page.component';
import { RepositorySettingsPageModule } from './repository-settings.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule, mockRepository } from '../../../../app-test.module';

describe('RepositorySettingsPage', () => {
  let component: RepositorySettingsPage;
  let fixture: ComponentFixture<RepositorySettingsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositorySettingsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepository(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositorySettingsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
