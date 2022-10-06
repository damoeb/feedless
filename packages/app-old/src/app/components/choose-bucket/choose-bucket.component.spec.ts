import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChooseBucketComponent } from './choose-bucket.component';
import { ChooseBucketModule } from './choose-bucket.module';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('ChooseBucketComponent', () => {
  let component: ChooseBucketComponent;
  let fixture: ComponentFixture<ChooseBucketComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ChooseBucketModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ChooseBucketComponent);
      component = fixture.componentInstance;
      component.buckets = [];
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
