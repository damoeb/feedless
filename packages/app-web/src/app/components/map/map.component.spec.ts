import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MapComponent } from './map.component';
import { AppTestModule } from '../../app-test.module';
import { LatLon } from '../../types';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [MapComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    const latLon: LatLon = {
      lat: 0,
      lon: 0,
    };
    componentRef.setInput('position', latLon);
    componentRef.setInput('perimeter', 10);
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
