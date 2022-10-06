import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FolderPage } from './folder.page';
import { FolderPageModule } from './folder.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('FolderPage', () => {
  let component: FolderPage;
  let fixture: ComponentFixture<FolderPage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [FolderPageModule, RouterTestingModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(FolderPage);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
