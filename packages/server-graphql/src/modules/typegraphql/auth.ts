import { Arg, Ctx, Mutation, Resolver } from 'type-graphql';
import { Logger } from '@nestjs/common';
import { AuthService } from '../../services/auth/auth.service';

@Resolver()
export class Auth {
  private readonly logger = new Logger(Auth.name);

  @Mutation(() => String)
  async getOauthRedirect(@Ctx() context: any): Promise<string> {
    this.logger.log(`getOauthRedirect`);
    const authService: AuthService = context.authService;
    return authService.getOauthRedirect();
  }
}
