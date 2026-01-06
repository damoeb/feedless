import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MapComponent } from './map.component';
import { AppTestModule } from '@feedless/test';
import { LatLng } from '@feedless/shared-types';

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
    const latLon: LatLng = {
      lat: 0,
      lng: 0,
    };
    componentRef.setInput('position', latLon);
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
