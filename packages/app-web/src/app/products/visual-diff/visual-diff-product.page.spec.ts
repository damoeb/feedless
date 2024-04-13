import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { VisualDiffProductPage } from './visual-diff-product.page';
import { ApolloMockController, AppTestModule, mockLicense, mockServerSettings } from '../../app-test.module';
import { VisualDiffProductModule } from './visual-diff-product.module';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('VisualDiffProductPage', () => {
  let component: VisualDiffProductPage;
  let fixture: ComponentFixture<VisualDiffProductPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualDiffProductModule, AppTestModule.withDefaults(apolloMockController => {
        mockLicense(apolloMockController)
      })],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(VisualDiffProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
