import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketsPage } from './buckets.page';
import { BucketsPageModule } from './buckets.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('BucketsPage', () => {
  let component: BucketsPage;
  let fixture: ComponentFixture<BucketsPage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [BucketsPageModule, RouterTestingModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(BucketsPage);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
