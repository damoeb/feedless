import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SourcesComponent } from './sources.component';
import { AppTestModule } from '../../app-test.module';
import { ModalService } from '../../services/modal.service';
import { RepositorySource } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';

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
      const modalService = TestBed.inject(ModalService);
      openFeedBuilderSpy = jest
        .spyOn(modalService, 'openFeedBuilder')
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
