import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { MockInstance } from 'vitest';

import { SourcesComponent } from './sources.component';
import { AppTestModule } from '@feedless/testing';
import { ModalProvider } from '../../modals';
import { RepositoryService } from '../../services';
import { RepositorySource } from '@feedless/graphql-api';

describe('SourcesComponent', () => {
  let component: SourcesComponent;
  let fixture: ComponentFixture<SourcesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SourcesComponent, AppTestModule.withDefaults()],
      providers: [
        {
          provide: ModalProvider,
          useValue: { openFeedBuilder: vi.fn().mockResolvedValue(undefined) },
        },
      ],
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
    let openFeedBuilderSpy: MockInstance;

    beforeEach(() => {
      const repositoryService = TestBed.inject(RepositoryService);
      vi
        .spyOn(repositoryService, 'getSourceFullByRepository')
        .mockResolvedValue({} as any);
      const modalProvider = TestBed.inject(ModalProvider);
      openFeedBuilderSpy = vi
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
