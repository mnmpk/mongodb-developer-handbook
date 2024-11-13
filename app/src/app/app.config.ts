import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideEcharts } from 'ngx-echarts';
import { loaderInterceptor } from './shared/loader/loader.interceptor';
import { provideApollo } from 'apollo-angular';
import { HttpLink, InMemoryCache, split } from '@apollo/client/core';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { getMainDefinition } from '@apollo/client/utilities';
import { createClient } from 'graphql-ws';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes), provideAnimationsAsync(),
    importProvidersFrom(HttpClientModule),
    provideHttpClient(withInterceptors([loaderInterceptor])),
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
              url: 'http://localhost:4000/',
            }),
          ),
          new HttpLink({
            uri: 'http://localhost:4000/',
          }),
        ),
        cache: new InMemoryCache(),
      };
    }),
  ]
};

