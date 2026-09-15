# Angular / Nx Rules

Two Angular clients: `packages/app-web` (Angular 20.3, Ionic 8, **yarn**) and `packages/frontend` (Nx 22, Angular 21, Ionic 8, **npm**). Both use Apollo Client over `graphql-ws`, Jest, and GraphQL codegen against the same schema.

Related: [`kotlin-spring.md`](kotlin-spring.md) (the schema's other consumer), [`../../AGENTS.md`](../../AGENTS.md) (hub).

## Which client am I in?

There is no single frontend. Check before editing — the two do not share code, tooling, or a package manager.

| | `app-web` | `frontend` |
|---|---|---|
| Verticals | feedless, rss-proxy, page-change-tracker, untold-notes, reader, visual-diff | upcoming, feed-reader, auction-alert |
| Angular / Nx | 20.3, no Nx | 21.0, Nx 22.3 |
| Package manager | **yarn** (`yarn.lock`) | **npm** (`package-lock.json`) |
| Layout | one app, `src/app/{components,pages,modals,guards,elements,directives}` | `apps/<name>` + `libs/@feedless/*` |
| E2E | `e2e/` | Playwright, `apps/<name>-e2e` |

**Why it matters:** running `npm install` in `app-web` writes a lockfile the Gradle build does not use — it runs `yarn install --frozen-lockfile`. The install succeeds and the CI build then fails on a lockfile nobody meant to change.

## CRITICAL: Never run bare `ng serve`

This is AGENTS.md Critical Rule #3.

The vertical is not a build flag. `prestart:<vertical>` runs `generate-dev-app-config.sh <vertical>` which writes `src/config.json`; `start:<vertical>` is then a plain `ng serve` that reads it.

### Wrong ❌

```bash
cd packages/app-web && ng serve          # serves whatever vertical was configured last
```

### Correct ✅

```bash
cd packages/app-web && yarn start:feedless      # prestart writes config.json, then serves
cd packages/frontend && npm run start:upcoming
```

**Why:** the wrong-vertical run compiles and boots cleanly. The symptom is missing features or wrong branding, which reads as a regression in a product you never touched.

Verticals are declared in `app-web/src/app/all-verticals.ts` (`VerticalSpec`: id, product, domain, phase, features, links). Adding a product means adding a spec plus its `prestart:`/`start:` script pair — not a new app.

## CRITICAL: `src/generated/graphql.ts` is generated

This is AGENTS.md Critical Rule #2, frontend half.

`app-web`, `browser-automation-app`, and `frontend/libs/graphql-api` each generate a TS client from `packages/graphql-api/src/main/resources/schema/schema.graphqls` via `codegen.yml` (`typescript`, `typescript-operations`, `typescript-document-nodes`; `app-web` prefixes types `Gql`).

After a schema change, regenerate in **every** consumer:

```bash
(cd packages/app-web && yarn codegen)
(cd packages/browser-automation-app && yarn codegen)
(cd packages/frontend && npm run codegen)
```

**Why:** `overwrite: true` — a hand edit is silently discarded on the next build. And `app-web`'s Gradle `codegen` task declares the schema as an input, so CI regenerates whether you did or not.

Write `.graphql` documents next to the code that uses them (`documents: ['./src/**/*.graphql']`); the generated hooks and types follow.

## Components

- **Standalone by default.** `app-web` has 114 standalone components against 4 remaining `NgModule`s. Do not add a component to a module — declare its imports on the component.
- Use Ionic components (`@ionic/angular`) before writing a layout primitive.
- `frontend` libs are imported by path alias only — `@feedless/components`, `@feedless/core`, `@feedless/geo`, `@feedless/graphql-api`, `@feedless/guards`, `@feedless/testing`. (`libs/design-system/` exists but has **no** path alias in `tsconfig.base.json` — it is not importable as `@feedless/design-system`.) Never reach into `libs/<name>/src/...` directly; the alias is the public surface, `index.ts` is the barrel that defines it.
- Nx's `@nx/enforce-module-boundaries` is enabled but **unconstrained** (`sourceTag: '*'` → `onlyDependOnLibsWithTags: ['*']`). Import discipline here is convention, not enforcement — the linter will not catch a lib reaching into an app.

## Tests

- Both use Jest with `jest-preset-angular`. `app-web`: `yarn test`. `frontend`: `npm test`, or `npx nx test <project>` for one project.
- `frontend` E2E is Playwright under `apps/<app>-e2e`. It is **not** part of `./gradlew lint test`.
- `lint` in both is what the CI gate actually runs (`./gradlew lint` reaches only these TS modules — AGENTS.md #4). Run it before pushing; it is the one linter in the repo.

Write the failing test before the implementation.
