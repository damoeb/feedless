import { Controller, Get, HttpException, HttpStatus } from '@nestjs/common';
import { SocketSubscriptionService } from './services/socket-subscription/socket-subscription.service';
import { VerboseConfigService } from './services/common/verbose-config.service';

@Controller()
export class AppController {
  constructor(
    private readonly socketSubscriptionService: SocketSubscriptionService,
    private readonly config: VerboseConfigService,
  ) {}

  @Get('readiness')
  async readiness() {
    return true;
  }

  @Get('liveness')
  async liveness() {
    const isLive = this.isLife();

    if (!isLive) {
      throw new HttpException(
        'Service is not live',
        HttpStatus.SERVICE_UNAVAILABLE,
      );
    }

    return true;
  }

  private isLife(): boolean {
    const isSocketDisabled = this.config.getBoolean(
      'APP_DISABLE_SOCKET_SUBSCRIPTION',
    );
    const isSocketConnected =
      this.socketSubscriptionService.isSocketConnected();
    return isSocketDisabled || isSocketConnected;
  }
}
