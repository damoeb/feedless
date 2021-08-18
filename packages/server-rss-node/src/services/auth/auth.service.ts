import { Injectable, Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { pick } from 'lodash';
import { PrismaService } from '../../modules/prisma/prisma.service';

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly jwtService: JwtService,
    private readonly prisma: PrismaService,
  ) {}

  async getOauthRedirect(): Promise<string> {
    const user = await this.prisma.user.findUnique({
      where: {
        email: 'karl@may.ch',
      },
    });
    const token = this.jwtService.sign(pick(user, ['id', 'email', 'name']));
    // this.logger.log(`${email} -> http://localhost:4200/authenticate/${token}`);
    return `/login/${token}`;
  }
}
