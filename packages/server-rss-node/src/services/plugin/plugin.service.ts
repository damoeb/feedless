import { Injectable, Logger } from '@nestjs/common';
import { watch, readdir, accessSync, constants } from 'fs';
import { debounce, groupBy } from 'lodash';
import { PrismaService } from '../../modules/prisma/prisma.service';
import { EventsService } from '../events/events.service';

export enum EventHookType {
  rootScript = 'rootScript',
  script = 'script',
  url = 'webhook',
}

@Injectable()
export class PluginService {
  private readonly pluginsFolder =
    '/home/damoeb/dev/private/rich-rss/packages/server-rss-node/plugins';
  private readonly logger = new Logger(PluginService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly events: EventsService,
  ) {
    this.init();
  }

  private async init() {
    await this.importPlugins();
    this.watchPluginFolder();
  }
  private async importPlugins() {
    try {
      await this.prisma.eventHook.deleteMany({
        where: {
          ownerId: 'system',
          type: EventHookType.rootScript,
        },
      });
      readdir(this.pluginsFolder, (err, files) => {
        if (err) {
          console.error('Cannot read plugin folder');
        } else {
          files.reduce(
            (waitFor, file) =>
              waitFor.then(() =>
                this.importPlugin(file).catch((e) =>
                  this.logger.error(e.message),
                ),
              ),
            Promise.resolve(),
          );
        }
      });
    } catch (e) {
      this.logger.error(`Error when importing plugins ${e.message}`);
    }
  }

  async importPlugin(file: string): Promise<void> {
    const [pluginName, eventsString] = file
      .replace(/\.[a-z]+$/, '')
      .split(' on ');
    const events = groupBy(eventsString.split(/[ ,;]+/), (event: string) =>
      this.events.exists(event),
    );
    if (events['false'] && events['false'].length > 0) {
      this.logger.error(
        `For plugin ${file} found unsupported events [${events['false']}]`,
      );
    }
    const validEvents: string[] = events['true'];
    if (!validEvents || validEvents.length === 0) {
      throw new Error(`Plugin '${pluginName}' has no valid events`);
    }

    const path = `${this.pluginsFolder}/${file}`;
    try {
      accessSync(path, constants.X_OK);
    } catch (e) {
      throw new Error(`Plugin ${file} is not executable, will be ignored`);
    }

    return validEvents.reduce(
      (waitFor, event) =>
        waitFor.then(async () => {
          await this.prisma.eventHook.create({
            data: {
              script_or_url: `file://${path}`,
              event,
              type: EventHookType.rootScript,
              owner: {
                connect: {
                  id: 'system',
                },
              },
            },
          });
          this.logger.log(
            `Loaded plugin '${pluginName}' for events [${validEvents.join(
              '/',
            )}] -> ${this.pluginsFolder}/${file}`,
          );
        }),
      Promise.resolve(),
    );
  }

  private watchPluginFolder() {
    watch(
      this.pluginsFolder,
      { encoding: 'utf8' },
      debounce(() => {
        this.importPlugins();
      }, 1000),
    );
  }
}
