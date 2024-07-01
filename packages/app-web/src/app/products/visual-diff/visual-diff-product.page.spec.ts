import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { VisualDiffProductPage } from './visual-diff-product.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { VisualDiffProductModule } from './visual-diff-product.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('VisualDiffProductPage', () => {
  let component: VisualDiffProductPage;
  let fixture: ComponentFixture<VisualDiffProductPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualDiffProductModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
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
