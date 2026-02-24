import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideEcharts } from 'ngx-echarts';
import { loaderInterceptor } from './shared/loader/loader.interceptor';
import { provideApollo } from 'apollo-angular';
import { HttpLink, InMemoryCache, split } from '@apollo/client/core';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { getMainDefinition } from '@apollo/client/utilities';
import { createClient } from 'graphql-ws';
import { environment } from '../environments/environment';
import { rxStompServiceFactory } from './shared/rx-stomp-service-factory';
import { RxStompService } from './shared/rx-stomp.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes), provideAnimationsAsync(),
    provideHttpClient(),
    provideHttpClient(withInterceptors([loaderInterceptor])),
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
    },
    provideEcharts(),
    provideApollo(() => {
      return {
        link: split(
          ({ query }) => {
            const definition = getMainDefinition(query);
            return definition.kind === 'OperationDefinition' && definition.operation === 'subscription';
          },
          new GraphQLWsLink(
            createClient({
              url: environment.graphQLUrl,
            }),
          ),
          new HttpLink({
            uri: environment.graphQLUrl,
          }),
        ),
        cache: new InMemoryCache(),
      };
    }),
  ]
};

