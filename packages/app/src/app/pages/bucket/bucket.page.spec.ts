import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketPage } from './bucket.page';
import { BucketPageModule } from './bucket.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('BucketPage', () => {
  let component: BucketPage;
  let fixture: ComponentFixture<BucketPage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [BucketPageModule, RouterTestingModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(BucketPage);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
