import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketSettingsComponent } from './bucket-settings.component';
import { BucketSettingsModule } from './bucket-settings.module';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('BucketSettingsComponent', () => {
  let component: BucketSettingsComponent;
  let fixture: ComponentFixture<BucketSettingsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [BucketSettingsModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(BucketSettingsComponent);
      component = fixture.componentInstance;
      component.bucket = { title: '', subscriptions: [] } as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
