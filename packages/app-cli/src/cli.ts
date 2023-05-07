import {writeFileSync, existsSync, readFileSync} from 'fs'
import {join} from 'path'
import * as winston from 'winston'
import {GraphqlClient} from 'client-lib'
import yargs, { Arguments, CommandModule, command } from 'yargs';

// curl -d @path/to/data.json https://reqbin.com/echo/post/json
// curl -X PUT -d '{"id": 1}' https://reqbin.com/echo/post/json

const toJSON = (obj: any): string => {
  return JSON.stringify(obj, null, 2)
}


const log = winston.createLogger({
  level: 'debug',
  // format: winston.format.json(),
  // defaultMeta: { service: 'user-service' },
  transports: [
    new winston.transports.Console({ format: winston.format.simple(), }),
  ],
});

interface Config {
  host: string
  token?: string
}

class Cli {
  private defaultConfig: Config = {
    host: 'http://localhost:8080'
  }

  private configPath = './feedless.json';
  private fullConfigPath = join(process.cwd(), this.configPath);
  private existsConfig = existsSync(this.configPath);
  private effectiveConfig: Config = this.existsConfig ? JSON.parse(readFileSync(this.configPath).toString()) : this.defaultConfig;

  private configOptions = {
    host: {
      alias: 'h',
      type: 'string',
      desc: 'Config param host',
      default: this.effectiveConfig.host,
      demandOption: false
    },
    token: {
      alias: 't',
      type: 'string',
      desc: 'Config param token',
      default: this.effectiveConfig.token,
      demandOption: false
    }
  }

  private graphqlClient: GraphqlClient;

  init() {
    require('yargs/yargs')(process.argv.slice(2))
      // .group('wefwef', 'dwefwef')
      // .command(['start [app]', 'run', 'up'], 'Start up an app', {}, (argv) => {
      //   console.log('starting up the', argv.app || 'default', 'app')
      // })
      .command({
        command: 'feed',
        aliases: ['feeds', 'f'],
        desc: 'Manage your feeds',
        builder: {
          ...this.configOptions,
          query: {
            alias: 'q',
            type: 'string',
            desc: 'GraphQL query',
          }
        },
        handler: (argv) => {
        }
      })
      .command({
        command: 'article',
        aliases: ['articles', 'a'],
        desc: 'Access articles',
        builder: {
          ...this.configOptions,
          query: {
            alias: 'q',
            type: 'string',
            desc: 'GraphQL query',
            demandOption: true
          }
        },
        handler: async (argv) => {
          await this.authenticate(argv.token, argv.host)
          const query = this.handleQuery(argv.query);
          log.debug(`query ${toJSON(query)}`)
          const articles = await this.graphqlClient.articles(query)
          console.log(articles)
        }
      })
      .command({
        command: 'update',
        aliases: ['u'],
        desc: 'Update cli',
        handler: (argv) => {
          console.log(argv)
        }
      })
      .command({
        command: 'config',
        aliases: ['cfg', 'c'],
        desc: 'Print or initialize config',
        builder: {
          init: {
            alias: 'i',
            default: false,
            type: 'boolean',
            demandOption: true
          }
        },
        handler: (argv) => {
          if (argv.init) {
            this.saveConfig(this.defaultConfig);
            if (this.existsConfig) {
              log.info(`Overwritten config ${this.fullConfigPath}`)
            } else {
              log.info(`Initialized config ${this.fullConfigPath}`)
            }
          } else {
            if (this.existsConfig) {
              log.info(readFileSync(this.configPath).toString())
              log.info(`Source: ${this.fullConfigPath}`)
            } else {
              log.info(`No config present. Add -init to create`)
            }
          }
        }
      })
      .option('debug')
      // .command({
      //   command: 'configure <key> [value]',
      //   aliases: ['config', 'cfg'],
      //   desc: 'Print config',
      //   handler: () => {
      //     console.log(readFileSync(path))
      //   }
      // })
      .demandCommand()
      .help()
      .version('0.1')
      .wrap(72)
      .argv
  }

  private async withoutToken(): Promise<void> {
    return this.graphqlClient.authenticateCli()
      .then(authentication => {
        this.effectiveConfig.token = authentication.token;
        this.saveConfig(this.effectiveConfig);
      })
  }

  private withToken(token: string): void {
    this.graphqlClient.authenticateCliWithToken(token)
  }

  private async authenticate(token: string | undefined, host: string | undefined) {
    this.initGraphqlClient(host);
    if (token) {
      this.withToken(token)
    } else {
      await this.withoutToken()
    }
  }

  private saveConfig(config: Config) {
    writeFileSync(this.configPath, toJSON(config))
    log.debug(`Updated config ${this.configPath}`)
  }

  private handleQuery(query: string) {
    if (query.startsWith('@')) {
      const pathToQuery = join(process.cwd(), query.substring(1));
      if (!existsSync(pathToQuery)) {
        throw new Error(`File '${pathToQuery}' not found`)
      }
      return JSON.parse(readFileSync(pathToQuery).toString())
    } else {
      return JSON.parse(query);
    }
  }

  private initGraphqlClient(host: string | undefined) {
    const parsed = new URL(host || this.effectiveConfig.host)
    this.graphqlClient = new GraphqlClient(parsed.host, parsed.protocol.endsWith('s'))
  }
}

new Cli().init()
