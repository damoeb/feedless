import { Observable, Subject } from 'rxjs';
import { VersionEvent } from '@angular/service-worker';

export class SwUpdateMock {
  // public available: Observable<UpdateAvailableEvent> = new Subject();
  // public activated: Observable<UpdateActivatedEvent> = new Subject();
  public versionUpdates: Observable<VersionEvent> = new Subject();
  public isEnabled: boolean = false;

  public checkForUpdate(): Promise<void> {
    return new Promise((resolve) => resolve());
  }
  public activateUpdate(): Promise<void> {
    return new Promise((resolve) => resolve());
  }
}
