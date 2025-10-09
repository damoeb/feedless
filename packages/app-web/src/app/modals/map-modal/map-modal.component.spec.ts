import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapModalComponent } from './map-modal.component';
import { AppTestModule } from '../../app-test.module';

describe('MapModalComponent', () => {
  let component: MapModalComponent;
  let fixture: ComponentFixture<MapModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(MapModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
