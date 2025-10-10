import { inject, Injectable } from '@angular/core';
import {
  CreateUserSecret,
  DeleteUserSecret,
  GqlCreateUserSecretMutation,
  GqlCreateUserSecretMutationVariables,
  GqlDeleteUserSecretInput,
  GqlDeleteUserSecretMutation,
  GqlDeleteUserSecretMutationVariables,
  GqlFeatureName,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlSessionQuery,
  GqlSessionQueryVariables,
  GqlUpdateCurrentUserInput,
  GqlUpdateCurrentUserMutation,
  GqlUpdateCurrentUserMutationVariables,
  Logout,
  Session as SessionQuery,
  UpdateCurrentUser,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Feature, Product, Session, UserSecret } from '../graphql/types';
import { BehaviorSubject, filter, Observable, ReplaySubject } from 'rxjs';
import { AppConfigService } from './app-config.service';
import { isNonNull, Nullable } from '../types';

export const dateFormat = 'dd.MM.yyyy';
export const dateTimeFormat = 'HH:mm, dd.MM.yyyy';
export const TimeFormat = 'HH:mm, dd.MM.yyyy';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly authService = inject(AuthService);
  private readonly appConfigService = inject(AppConfigService);

  private session: Session = {} as any;
  private darkModePipe: ReplaySubject<boolean>;
  private sessionPipe: BehaviorSubject<Nullable<Session>>;

  constructor() {
    this.darkModePipe = new ReplaySubject<boolean>(1);
    this.sessionPipe = new BehaviorSubject<Nullable<Session>>(null);
    this.detectColorScheme();
  }

  getSession(): Observable<Session> {
    return this.sessionPipe.asObservable().pipe(filter(isNonNull));
  }

  watchColorScheme(): Observable<boolean> {
    return this.darkModePipe.asObservable();
  }

  setColorScheme(dark: boolean): void {
    this.darkModePipe.next(dark);
  }

  getFeature(featureName: GqlFeatureName): Nullable<Feature> {
    return this.session?.user?.features.find((ft) => ft.name === featureName)!;
  }

  async fetchSession(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
    await this.apollo
      .query<GqlSessionQuery, GqlSessionQueryVariables>({
        query: SessionQuery,
        variables: {},
        fetchPolicy,
      })
      .then((response) => response.data.session)
      .then(async (session) => {
        this.session = session;
        this.sessionPipe.next(session);

        if (session.isLoggedIn) {
          this.authService.changeAuthStatus(session.isLoggedIn);
        }
      });
  }

  async finalizeSignUp(product: Nullable<Product> = null): Promise<void> {
    const { dateFormat, timeFormat } = this.getBrowserDateTimeFormats();
    const data: GqlUpdateCurrentUserMutationVariables['data'] = {
      acceptedTermsAndServices: {
        set: true,
      },
      timeFormat: {
        set: timeFormat,
      },
      dateFormat: {
        set: dateFormat,
      },
    };
    if (product) {
      data['plan'] = {
        set: product.id,
      };
    }

    await this.updateCurrentUser(data).then(() => this.fetchSession('network-only'));
  }

  async updateCurrentUser(data: GqlUpdateCurrentUserInput): Promise<void> {
    await this.apollo
      .mutate<GqlUpdateCurrentUserMutation, GqlUpdateCurrentUserMutationVariables>({
        mutation: UpdateCurrentUser,
        variables: {
          data,
        },
      })
      .then(() => this.fetchSession('network-only'));
  }

  async createUserSecret(): Promise<UserSecret> {
    return this.apollo
      .mutate<GqlCreateUserSecretMutation, GqlCreateUserSecretMutationVariables>({
        mutation: CreateUserSecret,
      })
      .then((response) => response.data!.createUserSecret);
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout,
      })
      .then(() => new Promise((resolve) => setTimeout(resolve, 200)))
      .then(() => this.apollo.clearStore())
      .then(() => location.reload());
  }

  getUserId(): Nullable<string> {
    return this.session?.user?.id;
  }

  isAuthenticated(): boolean {
    let userId = this.getUserId();
    return userId && userId?.length > 0;
  }

  async deleteUserSecret(data: GqlDeleteUserSecretInput) {
    await this.apollo.mutate<GqlDeleteUserSecretMutation, GqlDeleteUserSecretMutationVariables>({
      mutation: DeleteUserSecret,
      variables: {
        data,
      },
    });
  }

  private detectColorScheme() {
    const isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.darkModePipe.next(isDarkMode);
  }

  private getBrowserDateTimeFormats() {
    const now = new Date(2013, 11, 31, 12, 1, 2);
    const dateFormat = now
      .toLocaleDateString()
      .replace('31', 'dd')
      .replace('12', 'MM')
      .replace('2013', 'yyyy');

    const timeFormat = now
      .toLocaleTimeString()
      .replace('12', 'HH')
      .replace('01', 'mm')
      .replace('AM', 'a');
    return { dateFormat, timeFormat };
  }
}
