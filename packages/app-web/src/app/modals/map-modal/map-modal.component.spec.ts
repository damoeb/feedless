import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapModalComponent } from './map-modal.component';
import { AppTestModule } from '../../app-test.module';
import { MapModalModule } from './map-modal.module';

describe('MapModalComponent', () => {
  let component: MapModalComponent;
  let fixture: ComponentFixture<MapModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(MapModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
