import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PlayerComponent } from './player.component';
import { PlayerModule } from './player.module';
import { AppTestModule } from '../../app-test.module';

describe('PlayerComponent', () => {
  let component: PlayerComponent;
  let fixture: ComponentFixture<PlayerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [PlayerModule, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(PlayerComponent);
    component = fixture.componentInstance;
    component.document = { enclosures: [] } as any;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
