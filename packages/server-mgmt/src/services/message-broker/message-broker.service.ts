import { Injectable, Logger } from '@nestjs/common';
import { without } from 'lodash';
import { connect, Connection, Channel } from 'amqplib';
import { ConfigService } from '@nestjs/config';
import { MqOperation } from '../../generated/mq';

@Injectable()
export class MessageBrokerService {
  private readonly logger = new Logger(MessageBrokerService.name);

  private connection: Connection;

  constructor(private config: ConfigService) {
    this.init();
  }

  exists(opName: string): boolean {
    return this.byName(opName) !== null;
  }
  byName(opName: string): MqOperation | null {
    const allOpNames = Object.keys(MqOperation);
    const matchingKey = allOpNames.find((key) => MqOperation[key] === opName);
    if (matchingKey) {
      return MqOperation[matchingKey];
    }
    return null;
  }

  private async init() {
    this.connection = await connect(
      this.config.get('RABBITMQ_URL') || 'amqp://localhost',
    );

    await Object.values(MqOperation).reduce((waitFor, op) => {
      return waitFor.then(async () => {
        try {
          const channel = await this.connection.createChannel();
          await channel.assertQueue(op, {
            durable: true,
            autoDelete: false,
            exclusive: false,
          });

          await channel.consume(
            op,
            (message) => {
              // this.logger.log(`-> ${op}`);
              try {
                this.notifyAll(
                  this.byName(op),
                  JSON.parse(message.content.toString()),
                );
              } catch (e) {
                this.notifyAll(this.byName(op), message.content.toString());
              }
            },
            { noAck: true },
          );
          this.publishers[op] = channel;
        } catch (e) {
          this.logger.error(`Failed to bootstrap ${op} cause ${e.message}`);
        }
      });
    }, Promise.resolve());
    this.logger.log(`Bootstrapping done`);
  }

  private readonly subscribers =
    MessageBrokerService.createSubscribersForEvents();
  private readonly publishers = {};

  private notifyAll(op: MqOperation, data: any) {
    this.subscribers[op].forEach((callback) => {
      callback(data);
    });
  }

  subscribe<T>(op: MqOperation, callback: (data: T) => void): void {
    this.logger.log(`subscribe ${op}`);
    this.subscribers[op].push(callback);
  }
  unsubscribe<T>(op: MqOperation, callback: (data: T) => void): void {
    this.logger.log(`unsubscribe ${op}`);
    this.subscribers[op] = without(this.subscribers[op], callback);
  }

  publish<T>(op: MqOperation, data: T): void {
    // this.logger.debug(`publish ${op}`);
    const channel: Channel = this.publishers[op];
    channel.sendToQueue(op, Buffer.from(JSON.stringify(data)));
  }

  private static createSubscribersForEvents() {
    return Object.values(MqOperation).reduce((all, opName) => {
      all[opName] = [];
      return all;
    }, {});
  }
}
