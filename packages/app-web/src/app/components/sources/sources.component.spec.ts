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
    let openFeedBuilderSpy: jasmine.Spy<ModalService[keyof ModalService]>;

    beforeEach(() => {
      const repositoryService = TestBed.inject(RepositoryService);
      spyOn(repositoryService, 'getSourceFullByRepository').and.returnValue(Promise.resolve({ } as any));
      const modalService = TestBed.inject(ModalService);
      openFeedBuilderSpy = spyOn(modalService, 'openFeedBuilder').and.returnValue(Promise.resolve());
    });

    it('for add source', async () => {
      await component.editOrAddSource();

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });

    it('for edit source', async () => {
      const source: RepositorySource = {id:''} as any;
      await component.editOrAddSource(source);

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });

  })

});
