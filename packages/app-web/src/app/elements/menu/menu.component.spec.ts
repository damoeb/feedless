import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MenuComponent } from './menu.component';

import { AppTestModule } from '../../app-test.module';

describe('MenuComponent', () => {
  let component: MenuComponent<any>;
  let fixture: ComponentFixture<MenuComponent<any>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(MenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
