import { Injectable, Logger } from '@nestjs/common';
import { without } from 'lodash';
import { connect, Connection, Channel } from 'amqplib';
import { ConfigService } from '@nestjs/config';

export enum EventType {
  articleHarvested = 'articleHarvested',
  readability = 'readability',
  readabilityParsed = 'readabilityParsed',
  readabilityFailed = 'readabilityFailed',
}

@Injectable()
export class MessageBrokerService {
  private readonly logger = new Logger(MessageBrokerService.name);

  private connection: Connection;
  constructor(private config: ConfigService) {
    this.init();
  }

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

  private async init() {
    this.connection = await connect(
      this.config.get('RABBITMQ_URL') || 'amqp://localhost',
    );

    await Object.keys(EventType).reduce((waitFor, event) => {
      return waitFor.then(async () => {
        try {
          this.logger.debug(`Bootstrapping ${event}`);
          const channel = await this.connection.createChannel();
          await channel.assertQueue(event, {
            durable: true,
            autoDelete: false,
            exclusive: false,
          });

          await channel.consume(
            event,
            (message) => {
              this.logger.log(`-> ${event}`);
              try {
                this.notifyAll(
                  EventType[event],
                  JSON.parse(message.content.toString()),
                );
              } catch (e) {
                this.notifyAll(EventType[event], message.content.toString());
              }
            },
            { noAck: true },
          );
          this.publishers[event] = channel;
        } catch (e) {
          this.logger.error(`Failed to bootstrap ${event} cause ${e.message}`);
        }
      });
    }, Promise.resolve());
    this.logger.log(`Bootstrapping done`);
  }

  private readonly subscribers =
    MessageBrokerService.createSubscribersForEvents();
  private readonly publishers = {};

  private notifyAll(event: EventType, data: any) {
    this.subscribers[event].forEach((callback) => {
      callback(data);
    });
  }

  subscribe<T>(event: EventType, callback: (data: T) => void): void {
    this.logger.log(`subscribe ${event}`);
    this.subscribers[event].push(callback);
  }
  unsubscribe<T>(event: EventType, callback: (data: T) => void): void {
    this.logger.log(`unsubscribe ${event}`);
    this.subscribers[event] = without(this.subscribers[event], callback);
  }

  publish<T>(event: EventType, data: T): void {
    this.logger.log(`publish ${event}`);
    const channel: Channel = this.publishers[event];
    channel.sendToQueue(event, Buffer.from(JSON.stringify(data)));
  }

  private static createSubscribersForEvents() {
    return Object.keys(EventType).reduce((all, eventName) => {
      all[eventName] = [];
      return all;
    }, {});
  }
}
