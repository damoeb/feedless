import { Controller, Get, Logger, Query, Res } from '@nestjs/common';
import { Response } from 'express';
import { ReadabilityService } from '../../../services/readability/readability.service';

@Controller('articles')
export class ArticleController {
  private readonly logger = new Logger(ArticleController.name);

  constructor(private readonly readabilityService: ReadabilityService) {}

  @Get('readability')
  async parseReadability(
    @Query('url') url: string,
    @Res() res: Response,
  ): Promise<void> {
    const readability = await this.readabilityService.getReadability(url);
    if (readability) {
      this.logger.debug(`ok -> parseReadability url=${url}`);
    } else {
      this.logger.debug(`not ok -> parseReadability url=${url}`);
    }
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(readability));
  }
}
