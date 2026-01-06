import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SourcesComponent } from './sources.component';
import { AppTestModule } from '@feedless/test';
import { ModalProvider } from '../console-button/console-button.component';
import { RepositoryService } from '@feedless/services';
import { RepositorySource } from '@feedless/graphql-api';

describe('SourcesComponent', () => {
  let component: SourcesComponent;
  let fixture: ComponentFixture<SourcesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SourcesComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SourcesComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('repository', { sources: [] });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('feed-builder-modal is openened', () => {
    let openFeedBuilderSpy: jest.SpyInstance;

    beforeEach(() => {
      const repositoryService = TestBed.inject(RepositoryService);
      jest
        .spyOn(repositoryService, 'getSourceFullByRepository')
        .mockResolvedValue({} as any);
      const modalProvider = TestBed.inject(ModalProvider);
      openFeedBuilderSpy = jest
        .spyOn(modalProvider, 'openFeedBuilder')
        .mockResolvedValue();
    });

    it('for add source', async () => {
      await component.editOrAddSource();

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });

    it('for edit source', async () => {
      const source: RepositorySource = { id: '' } as any;
      await component.editOrAddSource(source);

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });
  });
});
