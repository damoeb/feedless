# testing

This library provides test utilities and mocks for the feedless frontend.

It was extracted from `@feedless/core` to break a circular dependency between `core` and `services`.

## Usage

```typescript
import {
  AppTestModule,
  ApolloMockController,
  mockServerSettings,
} from '@feedless/testing';
```

## Contents

- `AppTestModule` - Angular test module with common providers
- `ApolloMockController` - Mock controller for Apollo GraphQL queries/mutations
- `mockServerSettings` - Helper to mock server settings
- Various mock helpers: `mockProducts`, `mockPlans`, `mockPlugins`, `mockRecords`, etc.


