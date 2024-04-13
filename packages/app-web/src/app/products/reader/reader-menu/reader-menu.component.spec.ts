import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ReaderMenuComponent } from './reader-menu.component';
import { ReaderMenuModule } from './reader-menu.module';

describe('ReaderMenuComponent', () => {
  let component: ReaderMenuComponent;
  let fixture: ComponentFixture<ReaderMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ReaderMenuModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
