import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportersComponent } from './importers.component';
import { ImportersModule } from './importers.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  GqlImportersQuery,
  GqlImportersQueryVariables,
  Importers,
} from '../../../generated/graphql';

describe('ImportersComponent', () => {
  let component: ImportersComponent;
  let fixture: ComponentFixture<ImportersComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ImportersModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlImportersQuery, GqlImportersQueryVariables>(Importers)
            .and.resolveOnce(async () => {
              return {
                data: {
                  importers: {
                    importers: [],
                    pagination: {} as any,
                  },
                },
              };
            });
        }),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
