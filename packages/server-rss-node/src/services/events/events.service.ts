import { Injectable } from '@nestjs/common';

export enum EventType {
  articleHarvested = 'articleHarvested',
}

@Injectable()
export class EventsService {
  exists(event: string): boolean {
    return this.byName(event) !== null;
  }
  byName(eventName: string): EventType | null {
    const key = Object.keys(EventType).find(
      (key) => EventType[key] === eventName,
    );
    if (key) {
      return EventType[key];
    }
    return null;
  }
}
