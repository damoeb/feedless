# Feed Builder Flow Buttons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace accordion headers with three aspect buttons (Source → Map → Reduce) that select which slice of the `SourceInput` JSON the user edits; previews show the outcome of that slice.

**Architecture:** The **single source of truth** is `GqlSourceInput` (see `packages/graphql-api/src/main/resources/schema/schema.graphqls`). `SourceBuilder` owns and mutates that JSON via `build()`. Flow buttons are **aspect selectors** — they do not own data, only choose which JSON paths are visible/editable. Three aspects map to `SourceInput` structure:

```
SourceInput {
  title, tags, latLng, draft          ← Source aspect
  flow: {
    sequence: [
      { fetch: { get: { url, forcePrerender, ... } } },     ← Source aspect
      { execute: { pluginId: org_feedless_feed, params: { org_feedless_feed: { generic: SelectorsInput } } } },  ← Map aspect
      { execute: { pluginId: org_feedless_filter, params: { org_feedless_filter: [ItemFilterParamsInput!] } } }, ← Reduce aspect
    ]
  }
}
```

Panels are views over `SourceBuilder` — all writes go through existing methods (`patch`, `patchFetch`, `addOrUpdatePluginById`, `removePluginById`). Field edits mark the pipeline **dirty** (`sourceBuilder.events.stateChange.next('DIRTY')`) and show the existing refresh bubble; they do **not** auto-trigger a preview scrape. Scrape runs on: explicit "Preview Feed" click, switching to Reduce aspect, or opening the feed preview tab when `shouldRefresh` is set.

An **alternative raw JSON view** (dev/advanced) lets users inspect and edit `SourceBuilder.build()` directly — complement to aspect views, not a replacement. Gate behind `appDev` directive or a "JSON" segment in the preview toolbar.

**Tech Stack:** Angular 20, Ionic 8, `GqlSourceInput` / `SourceBuilder`, `InteractiveWebsiteComponent`, `RemoteFeedPreviewComponent`, Jest.

---

## Schema → Aspect Mapping

Reference: `schema.graphqls` lines 1650–1690, 831–838, 1669–1690, 1678–1680, 1434–1441.

| Aspect ID | Button | JSON paths edited | `SourceBuilder` API | Configured when |
|-----------|--------|-------------------|---------------------|-----------------|
| `source` | Source | `title`, `tags`, `latLng`, `flow.sequence[0].fetch.get` | `patch()`, `patchFetch()` | URL set or metadata filled |
| `map` | Map | `flow.sequence[*].execute` where `pluginId === org_feedless_feed` → `params.org_feedless_feed.generic` (`SelectorsInput`). Native feeds also replace `fetch.get.url` with the RSS feed URL. | `addOrUpdatePluginById(OrgFeedlessFeed, …)`, `patchFetch()` | Generic: `contextXPath` set. Native: user confirmed feed selection and `fetch.get.url` now points to RSS feed URL |
| `reduce` | Reduce | `flow.sequence[*].execute` where `pluginId === org_feedless_filter` → `params.org_feedless_filter` (`ItemFilterParamsInput[]`) | `addOrUpdatePluginById(OrgFeedlessFilter, …)`, `removePluginById()` | Valid filter entries or expression |

**Preview per aspect** always scrapes the **full** `SourceBuilder.build()` pipeline (fetch → feed → filter). Aspect buttons do not truncate the scrape — they only control which left panel is visible and which right-hand preview is emphasized (`website`, `both`, or `feed` via `previewModeForAspect()`).

- **Source** → interactive website (fetch output emphasized)
- **Map** → interactive website + feed tab when `org_feedless_feed` is configured
- **Reduce** → feed preview emphasized (filtered items from full pipeline)

Discovery UI inside Map (native/generic feed picker) is a **helper** that populates `SelectorsInput` / feed plugin — it is not separate state.

**URL changes do not auto-invalidate** downstream plugins. If the user edits the Source URL after configuring Map or Reduce, `org_feedless_feed` and `org_feedless_filter` remain in `flow.sequence`; the user must manually adjust xpaths/filters if they no longer match. No confirm dialog, no plugin clearing.

**Open:** Repository-modal filter accordion unification — deferred to follow-up PR. This refactor only changes feed-builder; `FilterItemsAccordionComponent` in repository-modal stays as-is until brought together later.

---

## File Structure

| File | Responsibility |
|------|----------------|
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow.types.ts` | `FeedBuilderAspectId`, aspect config, JSON path constants, preview mode |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow-nav.component.*` | Aspect selector buttons |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-source-aspect.component.*` | View over `SourceInput` metadata + fetch action |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-map-aspect.component.*` | View over `org_feedless_feed` + feed discovery helpers |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-reduce-aspect.component.*` | View over `org_feedless_filter` |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-feeds-panel.component.*` | Feed discovery helper (writes to Map aspect JSON) |
| `packages/app-web/src/app/components/feed-builder-flow/feed-builder-customize-panel.component.*` | `SelectorsInput` editor (writes to Map aspect JSON) |
| `packages/app-web/src/app/components/filter-items-accordion/filter-items-panel.component.*` | `ItemFilterParamsInput[]` editor (used in Reduce aspect) |
| `packages/app-web/src/app/components/interactive-website/source-builder.ts` | **Model** — extend only if aspect helpers needed (e.g. `getAspectState()`) |
| `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.*` | Active aspect, preview orchestration |
| `packages/app-web/src/app/components/feed-builder/feed-builder.component.*` | Hosts `SourceBuilder`, passes to transform component |

**Out of scope (follow-up):** `packages/frontend/libs/components/src/lib/components/feed-builder/` — mirror changes after app-web is stable.

---

## Aspect → View Mapping

Three buttons select which **aspect** of `GqlSourceInput` is in focus. Each aspect renders one view group (may contain multiple sub-sections for related JSON fields).

| Aspect ID | Button | View (left) | Preview (right) |
|-----------|--------|-------------|-----------------|
| `source` | Source | `FeedBuilderSourceAspectComponent` — binds `title`/`tags`/`latLng` + `fetch.get` | Interactive website (fetch output) |
| `map` | Map | `FeedBuilderMapAspectComponent` — feed discovery + `SelectorsInput` editor for `org_feedless_feed.generic` | Interactive website + feed items when `org_feedless_feed` configured |
| `reduce` | Reduce | `FeedBuilderReduceAspectComponent` — composite + expression `org_feedless_filter` entries | Filtered feed items |

Aspect `reduce` is disabled until Map aspect has a valid `org_feedless_feed` (same as current `hasValidFeed`).

### Single active aspect (required)

Unlike the current accordions (`[multiple]="true"`), the flow nav is **exclusive**:

- `TransformWebsiteToFeedComponent` holds one `activeAspectId: FeedBuilderAspectId` (not an array).
- `setActiveAspect(id)` **replaces** the current aspect; it never toggles or accumulates.
- `FeedBuilderFlowNavComponent` derives button active styling from `activeAspectId === aspect.id`.
- The left content area uses `@switch (activeAspectId)` so exactly one aspect view renders.
- Clicking the already-active button is a no-op.
- **No auto-advance:** the UI never switches aspects automatically (not after URL scrape, not after feed pick). User always selects the next aspect manually.
- **Switch during scrape:** aspect buttons stay enabled while a scrape/preview is in flight. The in-flight request continues; when it completes, the preview updates for whichever aspect is currently active.
- **No parallel form state:** aspect components read/write `SourceBuilder` only. Remove duplicate fields from `FeedBuilderComponent` that mirror `SourceInput` (e.g. `titleFc`, `tags`, `geoLocation` should bind to `sourceBuilder.meta` or `patch()` directly).

### Initial aspect when editing existing source

When `FeedBuilderComponent` receives a `source` input (edit/remix flow), derive `activeAspectId` from JSON completeness — do not always default to `source`:

```typescript
export function deriveInitialAspect(builder: SourceBuilder, hasValidFeed: boolean): FeedBuilderAspectId {
  if (!isSourceAspectConfigured(builder)) return 'source';
  if (!isMapAspectConfigured(builder) || !hasValidFeed) return 'map';
  return 'reduce';
}
```

All fields populate from the existing `GqlSourceInput` via `SourceBuilder.fromSource()`.

### Narrow viewport layout

On narrow screens, stack vertically (replace side-by-side columns):

1. Aspect nav (horizontal, top)
2. Active aspect form (scrollable)
3. Preview panel (website or feed, below form)

Use existing `app-responsive-columns` breakpoint or a CSS `@media` rule in `transform-website-to-feed.component.scss`. Desktop keeps the current two-column layout (form+nav left, preview right).

### Scrape/preview errors

When `scrapeService.scrape()` fails, attribute the error to the **active aspect** at failure time:

- Set `hasError: true` on that aspect in `buildAspects()` (red bubble on the aspect button via `app-bubble`)
- Show error message/details in the **right preview panel** (not inline in the left form)
- Left aspect form stays editable so the user can fix and retry
- Clear `hasError` on the aspect when the next successful scrape completes

```typescript
protected aspectErrors: Partial<Record<FeedBuilderAspectId, string>> = {};

// in fetchFeedPreview catch block:
this.aspectErrors[this.activeAspectId] = e.message;
// in success:
delete this.aspectErrors[this.activeAspectId];
```

Pass `aspectErrors` into `buildAspects()` to set `hasError` on matching aspects.

### Native feed selection requires confirmation

Picking a native feed changes `SourceInput.flow.sequence[0].fetch.get.url` to the RSS feed URL. Before applying, show an `AlertController` confirm:

> "Use [feed title] as source? The source URL will change to [feedUrl]."

On confirm → `patchFetch({ url: { literal: feed.feedUrl } })` + `addOrUpdatePluginById(OrgFeedlessFeed, …)`. On cancel → no changes.

---

### Task 1: Aspect types, JSON helpers, and nav component

**Files:**
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow.types.ts`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-aspect.utils.ts`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow-nav.component.ts`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow-nav.component.html`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow-nav.component.scss`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-flow-nav.component.spec.ts`

- [ ] **Step 1: Write the failing test**

```typescript
// feed-builder-flow-nav.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedBuilderFlowNavComponent } from './feed-builder-flow-nav.component';
import { FeedBuilderAspect, FeedBuilderAspectId } from './feed-builder-flow.types';

describe('FeedBuilderFlowNavComponent', () => {
  let fixture: ComponentFixture<FeedBuilderFlowNavComponent>;

  const aspects: FeedBuilderAspect[] = [
    { id: 'source', label: 'Source', enabled: true, configured: true },
    { id: 'map', label: 'Map', enabled: true, configured: false },
    { id: 'reduce', label: 'Reduce', enabled: false, configured: false },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedBuilderFlowNavComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderFlowNavComponent);
    fixture.componentRef.setInput('aspects', aspects);
    fixture.componentRef.setInput('activeAspectId', 'source');
    fixture.detectChanges();
  });

  it('renders a button per step in order', () => {
    const buttons = fixture.nativeElement.querySelectorAll('[data-flow-step]');
    expect(buttons.length).toBe(3);
    expect(buttons[0].textContent).toContain('Source');
    expect(buttons[1].textContent).toContain('Map');
  });

  it('emits aspectSelected when an enabled aspect is clicked', () => {
    const emitSpy = jest.spyOn(fixture.componentInstance.aspectSelected, 'emit');
    const mapButton = fixture.nativeElement.querySelector('[data-aspect="map"]');
    mapButton.click();
    expect(emitSpy).toHaveBeenCalledWith('map');
  });

  it('does not emit when a disabled aspect is clicked', () => {
    const emitSpy = jest.spyOn(fixture.componentInstance.aspectSelected, 'emit');
    const reduceButton = fixture.nativeElement.querySelector('[data-aspect="reduce"]');
    reduceButton.click();
    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('highlights exactly one button as active', () => {
    const activeButtons = fixture.nativeElement.querySelectorAll(
      '[data-aspect][data-active="true"]'
    );
    expect(activeButtons.length).toBe(1);
    expect(activeButtons[0].getAttribute('data-aspect')).toBe('source');
  });

  it('does not emit when clicking the already-active aspect', () => {
    const emitSpy = jest.spyOn(fixture.componentInstance.aspectSelected, 'emit');
    fixture.nativeElement.querySelector('[data-aspect="source"]').click();
    expect(emitSpy).not.toHaveBeenCalled();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder-flow-nav.component.spec.ts -v`

Expected: FAIL with "Cannot find module './feed-builder-flow-nav.component'"

- [ ] **Step 3: Write minimal implementation**

```typescript
// feed-builder-flow.types.ts
export type FeedBuilderAspectId = 'source' | 'map' | 'reduce';

export type FeedBuilderPreviewMode = 'website' | 'feed' | 'both';

export interface FeedBuilderAspect {
  id: FeedBuilderAspectId;
  label: string;
  enabled: boolean;
  configured: boolean;
  hasError?: boolean;
  badge?: string;
}
```

```typescript
// feed-builder-aspect.utils.ts
import { GqlFeedlessPlugins } from '../../../generated/graphql';
import { SourceBuilder } from '../interactive-website/source-builder';
import { FeedBuilderAspect, FeedBuilderAspectId } from './feed-builder-flow.types';

export function isSourceAspectConfigured(builder: SourceBuilder): boolean {
  const meta = builder.meta.value;
  return Boolean(
    builder.getUrl()?.length ||
      meta.title?.length ||
      meta.tags?.length ||
      meta.latLng ||
      builder.needsJavascript()
  );
}

export function isMapAspectConfigured(builder: SourceBuilder): boolean {
  const feedAction = builder.findFirstByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed);
  if (feedAction?.execute?.params?.org_feedless_feed?.generic?.contextXPath) {
    return true;
  }
  // Native feed: fetch URL was changed to RSS feed URL (after user confirmed pickNativeFeed)
  const fetchUrl = builder.getUrl();
  const originalUrl = builder.response?.outputs?.[0]?.response?.fetch?.debug?.url;
  if (feedAction && fetchUrl && originalUrl && fetchUrl !== originalUrl) {
    return true;
  }
  return false;
}

export function isReduceAspectConfigured(builder: SourceBuilder): boolean {
  const filters =
    builder.findFirstByPluginsId(GqlFeedlessPlugins.OrgFeedlessFilter)?.execute?.params
      ?.org_feedless_filter ?? [];
  return filters.some((f) => f.composite || f.expression?.trim());
}

export function buildAspects(
  builder: SourceBuilder,
  hasValidFeed: boolean
): FeedBuilderAspect[] {
  return [
    {
      id: 'source',
      label: 'Source',
      enabled: true,
      configured: isSourceAspectConfigured(builder),
    },
    {
      id: 'map',
      label: 'Map',
      enabled: true,
      configured: isMapAspectConfigured(builder),
    },
    {
      id: 'reduce',
      label: 'Reduce',
      enabled: hasValidFeed,
      configured: isReduceAspectConfigured(builder),
    },
  ];
}

export function previewModeForAspect(
  aspectId: FeedBuilderAspectId,
  hasSelectedFeed: boolean
): 'website' | 'feed' | 'both' {
  switch (aspectId) {
    case 'source':
      return 'website';
    case 'map':
      return hasSelectedFeed ? 'both' : 'website';
    case 'reduce':
      return 'feed';
  }
}

export function deriveInitialAspect(
  builder: SourceBuilder,
  hasValidFeed: boolean
): FeedBuilderAspectId {
  if (!isSourceAspectConfigured(builder)) return 'source';
  if (!isMapAspectConfigured(builder) || !hasValidFeed) return 'map';
  return 'reduce';
}
```

```typescript
// feed-builder-flow-nav.component.ts
import { FeedBuilderAspect, FeedBuilderAspectId } from './feed-builder-flow.types';
// ...
export class FeedBuilderFlowNavComponent {
  readonly aspects = input.required<FeedBuilderAspect[]>();
  readonly activeAspectId = input.required<FeedBuilderAspectId>();
  readonly aspectSelected = output<FeedBuilderAspectId>();

  selectAspect(aspect: FeedBuilderAspect) {
    if (aspect.enabled && aspect.id !== this.activeAspectId()) {
      this.aspectSelected.emit(aspect.id);
    }
  }

  isActive(aspect: FeedBuilderAspect): boolean {
    return this.activeAspectId() === aspect.id;
  }
}
```

```html
<!-- feed-builder-flow-nav.component.html -->
<div class="feed-builder-flow-nav" role="tablist" aria-label="Feed builder steps">
  @for (aspect of aspects(); track aspect.id; let last = $last) {
    <ion-button
      role="tab"
      [attr.aria-selected]="isActive(aspect)"
      [attr.data-active]="isActive(aspect)"
      [attr.data-aspect]="aspect.id"
      [color]="isActive(aspect) ? 'primary' : aspect.configured ? 'dark' : 'medium'"
      [disabled]="!aspect.enabled"
      [fill]="isActive(aspect) ? 'solid' : 'outline'"
      size="small"
      (click)="selectAspect(aspect)"
    >
      {{ aspect.label }}
      @if (aspect.hasError) {
        <app-bubble color="red" class="feed-builder-flow-nav__error-bubble"></app-bubble>
      }
      @if (aspect.badge) {
        <span class="feed-builder-flow-nav__badge">{{ aspect.badge }}</span>
      }
    </ion-button>
    @if (!last) {
      <ion-icon class="feed-builder-flow-nav__connector" name="chevron-forward"></ion-icon>
    }
  }
</div>
```

```scss
/* feed-builder-flow-nav.component.scss */
.feed-builder-flow-nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 12px;
}

.feed-builder-flow-nav__connector {
  color: var(--ion-color-medium);
  font-size: 14px;
}

.feed-builder-flow-nav__badge {
  margin-left: 6px;
  font-size: 11px;
  opacity: 0.8;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder-flow-nav.component.spec.ts -v`

Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/feed-builder-flow/
git commit -m "feat(feed-builder): add schema-driven aspect types and nav"
```

---

### Task 2: Source aspect view (`SourceInput` metadata + fetch)

**Files:**
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-source-aspect.component.*`
- Modify: `packages/app-web/src/app/components/feed-builder/feed-builder.component.ts` (pass `sourceBuilder`, remove duplicate `titleFc`/`tags`/`geoLocation` — bind via `sourceBuilder.meta` and `patch()`)

- [ ] **Step 1: Write the failing test**

```typescript
// feed-builder-source-aspect.component.spec.ts
import { FeedBuilderSourceAspectComponent } from './feed-builder-source-aspect.component';
import { SourceBuilder } from '../interactive-website/source-builder';

describe('FeedBuilderSourceAspectComponent', () => {
  // ...
  it('binds to SourceBuilder.meta for title/tags/latLng', () => {
    // sourceBuilder.meta holds SourceInput fields
    expect(fixture.nativeElement.textContent).toContain('Title');
  });
});
```

Key implementation — aspect receives `sourceBuilder` input, no duplicate state:

```typescript
// feed-builder-source-aspect.component.ts
readonly sourceBuilder = input.required<SourceBuilder>();
readonly loading = input(false);
readonly errorMessage = input<string | null>(null);

// URL from flow.sequence[0].fetch.get.url.literal via sourceBuilder.getUrl()
// title/tags/latLng from sourceBuilder.meta (maps to SourceInput)
// forcePrerender from sourceBuilder.needsJavascript() → patchFetch({ forcePrerender })

onUrlSubmit(url: string) {
  this.urlSubmit.emit(url); // parent calls sourceBuilder.patchFetch + scrape
}
```

Remove `titleFc`, `tags`, `geoLocation` from `FeedBuilderComponent`; use `sourceBuilder.meta` and `sourceBuilder.patch()` in `createOrRefineFeed()`.

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder-source-aspect.component.spec.ts -v`

Expected: FAIL with module not found

- [ ] **Step 3: Write minimal implementation**

Move accordion body from `feed-builder.component.html` lines 64–150 into `FeedBuilderSourceAspectComponent`. Template binds directly to `sourceBuilder()`:

- URL input → `sourceBuilder().getUrl()` / `patchFetch({ url: { literal } })`
- Title → `sourceBuilder().meta.controls.title`
- Tags → `sourceBuilder().meta.controls.tags` (open tags modal, then `patch({ tags })`)
- Geo → `sourceBuilder().meta.controls.latLng`
- JavaScript toggle → `patchFetch({ forcePrerender: true/false })`

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder-source-aspect.component.spec.ts -v`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/feed-builder-flow/feed-builder-source-aspect.*
git add packages/app-web/src/app/components/feed-builder/feed-builder.component.ts
git commit -m "feat(feed-builder): source aspect view over SourceInput JSON"
```

---

### Task 3: Map aspect view (`org_feedless_feed` + `SelectorsInput`)

**Files:**
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-feeds-panel.component.*`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-customize-panel.component.*`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-map-aspect.component.*`
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.ts` (expose data for panels)

- [ ] **Step 1: Write the failing test**

```typescript
// feed-builder-feeds-panel.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedBuilderFeedsPanelComponent } from './feed-builder-feeds-panel.component';

describe('FeedBuilderFeedsPanelComponent', () => {
  let fixture: ComponentFixture<FeedBuilderFeedsPanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedBuilderFeedsPanelComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderFeedsPanelComponent);
    fixture.componentRef.setInput('nativeFeeds', [{ title: 'Heise', feedUrl: 'https://heise.de/rss' }]);
    fixture.componentRef.setInput('genericFeeds', [{ hash: 'abc', count: 12, score: 0.8, selectors: {} }]);
    fixture.componentRef.setInput('selectedNativeFeed', null);
    fixture.componentRef.setInput('selectedGenericFeedHash', null);
    fixture.componentRef.setInput('getRelativeScore', () => 80);
    fixture.detectChanges();
  });

  it('lists native and generic feeds', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Native Heise');
    expect(el.textContent).toContain('Generic with 12 items');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder-feeds-panel.component.spec.ts -v`

Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

Extract feeds list from `transform-website-to-feed.component.html` lines 15–49 into `FeedBuilderFeedsPanelComponent`:

```typescript
// feed-builder-feeds-panel.component.ts
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NgStyle } from '@angular/common';
import { GqlRemoteNativeFeed, GqlTransientGenericFeed } from '../../../generated/graphql';
import { IonIcon, IonItem, IonLabel } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronForward } from 'ionicons/icons';

@Component({
  selector: 'app-feed-builder-feeds-panel',
  templateUrl: './feed-builder-feeds-panel.component.html',
  styleUrls: ['./feed-builder-feeds-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonItem, IonLabel, IonIcon, NgStyle],
  standalone: true,
})
export class FeedBuilderFeedsPanelComponent {
  readonly nativeFeeds = input<GqlRemoteNativeFeed[]>([]);
  readonly genericFeeds = input<GqlTransientGenericFeed[]>([]);
  readonly selectedNativeFeed = input<GqlRemoteNativeFeed | null>(null);
  readonly selectedGenericFeedHash = input<string | null>(null);
  readonly getRelativeScore = input.required<(feed: GqlTransientGenericFeed) => number>();

  readonly nativeFeedSelected = output<GqlRemoteNativeFeed>();
  readonly genericFeedSelected = output<GqlTransientGenericFeed>();

  constructor() {
    addIcons({ chevronForward });
  }
}
```

Extract customize form from `transform-website-to-feed.component.html` lines 61–176 into `FeedBuilderCustomizePanelComponent`:

```typescript
// feed-builder-customize-panel.component.ts
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Selectors } from '../../graphql/types';
import { TypedFormControls } from '../transform-website-to-feed/transform-website-to-feed.component';
import {
  IonButton,
  IonCheckbox,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonNote,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { searchOutline } from 'ionicons/icons';

@Component({
  selector: 'app-feed-builder-customize-panel',
  templateUrl: './feed-builder-customize-panel.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    IonItem,
    IonInput,
    IonButton,
    IonIcon,
    IonNote,
    IonCheckbox,
    IonLabel,
  ],
  standalone: true,
})
export class FeedBuilderCustomizePanelComponent {
  readonly sourceBuilder = input.required<SourceBuilder>();
  readonly pickWithin = output<{ xpath: string; field: keyof Selectors }>();

  selectors(): Selectors {
    return (
      this.sourceBuilder().findFirstByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed)?.execute
        ?.params?.org_feedless_feed?.generic ?? defaultSelectors()
    );
  }

  patchSelector(field: keyof Selectors, value: string | boolean) {
    const current = this.selectors();
    this.sourceBuilder().addOrUpdatePluginById(GqlFeedlessPlugins.OrgFeedlessFeed, {
      execute: {
        pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
        params: { org_feedless_feed: { generic: { ...current, [field]: value } } },
      },
    });
    this.sourceBuilder().events.stateChange.next('DIRTY'); // no auto-scrape
  }
}
```

Compose into `FeedBuilderMapAspectComponent`. Feed picker and xpath editor write directly to `sourceBuilder` — **no `genFeedXpathsFg` FormGroup**. `FeedBuilderCustomizePanelComponent` reads/writes `params.org_feedless_feed.generic` (`SelectorsInput`) via `addOrUpdatePluginById(OrgFeedlessFeed, …)` on each field change (debounced 200ms, same as current).

```typescript
// feed-builder-map-aspect.component.ts
@Component({
  selector: 'app-feed-builder-map-aspect',
  templateUrl: './feed-builder-map-aspect.component.html',
  imports: [FeedBuilderFeedsPanelComponent, FeedBuilderCustomizePanelComponent],
  standalone: true,
})
export class FeedBuilderMapAspectComponent {
  readonly sourceBuilder = input.required<SourceBuilder>();
  // No genFeedXpathsFg — customize panel binds SelectorsInput fields directly to SourceBuilder JSON
  // pickNativeFeed → confirm dialog, then patchFetch url + addOrUpdatePluginById
}
```

Remove `genFeedXpathsFg` from `TransformWebsiteToFeedComponent` entirely. `pickGenericFeed`, `customizeGenericFeed`, and `propagateCurrentGenFeed` refactor to read/write plugin params on `SourceBuilder` directly. `selectedFeed` remains a **derived view** for preview highlighting — rebuilt from JSON on init and after feed picker actions, not an independent edit buffer.

- [ ] **Step 4: Run tests**

Run: `cd packages/app-web && npm test -- --testPathPattern="feed-builder-feeds-panel|feed-builder-map-aspect" -v`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/feed-builder-flow/feed-builder-feeds-panel.*
git add packages/app-web/src/app/components/feed-builder-flow/feed-builder-customize-panel.*
git add packages/app-web/src/app/components/feed-builder-flow/feed-builder-map-aspect.*
git commit -m "feat(feed-builder): add map aspect view for org_feedless_feed JSON"
```

---

### Task 4: Reduce aspect view (`org_feedless_filter`)

**Scope note:** Only feed-builder uses the new `FeedBuilderReduceAspectComponent`. Repository-modal filter accordions stay unchanged in this PR — unification deferred.

**Files:**
- Create: `packages/app-web/src/app/components/filter-items-accordion/filter-items-panel.component.ts`
- Create: `packages/app-web/src/app/components/filter-items-accordion/filter-items-panel.component.html`
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-reduce-aspect.component.*`

- [ ] **Step 1: Write the failing test**

```typescript
// filter-items-panel.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FilterItemsPanelComponent } from './filter-items-panel.component';
import { AppTestModule } from '../../app-test.module';

describe('FilterItemsPanelComponent', () => {
  let fixture: ComponentFixture<FilterItemsPanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), FilterItemsPanelComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FilterItemsPanelComponent);
    fixture.componentRef.setInput('filterPlugin', []);
    fixture.componentRef.setInput('mode', 'composite');
    fixture.detectChanges();
  });

  it('renders Add Filter button in composite mode', () => {
    expect(fixture.nativeElement.textContent).toContain('Add Filter');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/app-web && npm test -- --testPathPattern=filter-items-panel.component.spec.ts -v`

Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

Move filter form markup from `filter-items-accordion.component.html` into new `filter-items-panel.component.html`. **Do not modify** `FilterItemsAccordionComponent` in this PR — feed-builder imports `FilterItemsPanelComponent` directly via `FeedBuilderReduceAspectComponent`. Repository-modal continues using the accordion wrapper until a follow-up unifies them.

```typescript
// filter-items-panel.component.ts — extract form logic from FilterItemsAccordionComponent
readonly mode = input<'composite' | 'expression' | 'all'>('all');
```

`FeedBuilderReduceAspectComponent` edits `params.org_feedless_filter` (`ItemFilterParamsInput[]`). On change → `sourceBuilder.addOrUpdatePluginById(GqlFeedlessPlugins.OrgFeedlessFilter, …)` or `removePluginById` when empty.

```typescript
// feed-builder-reduce-aspect.component.ts
@Component({
  selector: 'app-feed-builder-reduce-aspect',
  template: `
    <app-filter-items-panel
      [advanced]="advanced()"
      [filterPlugin]="filterPluginFromSource()"
      [mode]="'all'"
      (filterChange)="onFilterChange($event)"
    ></app-filter-items-panel>
  `,
  imports: [FilterItemsPanelComponent],
  standalone: true,
})
export class FeedBuilderReduceAspectComponent {
  readonly sourceBuilder = input.required<SourceBuilder>();

  filterPluginFromSource() {
    return this.sourceBuilder().findFirstByPluginsId(GqlFeedlessPlugins.OrgFeedlessFilter)
      ?.execute?.params?.org_feedless_filter;
  }

  onFilterChange(params: GqlItemFilterParamsInput[]) {
    if (params.length === 0) {
      this.sourceBuilder().removePluginById(GqlFeedlessPlugins.OrgFeedlessFilter);
    } else {
      this.sourceBuilder().addOrUpdatePluginById(GqlFeedlessPlugins.OrgFeedlessFilter, {
        execute: { pluginId: GqlFeedlessPlugins.OrgFeedlessFilter, params: { org_feedless_filter: params } },
      });
    }
    this.filterChange.emit(params);
  }
}
```

- [ ] **Step 4: Run tests**

Run: `cd packages/app-web && npm test -- --testPathPattern="filter-items-panel|feed-builder-reduce-aspect" -v`

Expected: PASS (accordion specs unchanged — not modified in this PR)

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/filter-items-accordion/
git add packages/app-web/src/app/components/feed-builder-flow/feed-builder-reduce-aspect.*
git commit -m "refactor(filters): add reduce aspect view for org_feedless_filter JSON"
```

---

### Task 5: Refactor transform-website-to-feed to aspect-driven layout

**Files:**
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.html`
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.ts`
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.scss`
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.spec.ts`

- [ ] **Step 1: Write the failing test**

```typescript
it('renders aspect nav instead of accordion group', () => {
  fixture.detectChanges();
  expect(fixture.nativeElement.querySelector('app-feed-builder-flow-nav')).toBeTruthy();
  expect(fixture.nativeElement.querySelector('ion-accordion-group')).toBeFalsy();
});

it('shows only one aspect view at a time', () => {
  component.activeAspectId = 'map';
  fixture.detectChanges();
  const views = fixture.nativeElement.querySelectorAll(
    'app-feed-builder-source-aspect, app-feed-builder-map-aspect, app-feed-builder-reduce-aspect'
  );
  expect(views.length).toBe(1);
});
```

- [ ] **Step 3: Write minimal implementation**

```html
<app-feed-builder-flow-nav
  [activeAspectId]="activeAspectId"
  [aspects]="aspects()"
  (aspectSelected)="setActiveAspect($event)"
></app-feed-builder-flow-nav>

<div class="feed-builder-aspect-content">
  @switch (activeAspectId) {
    @case ('source') {
      <app-feed-builder-source-aspect
        [errorMessage]="errorMessage"
        [loading]="loading"
        [sourceBuilder]="sourceBuilder()"
        (urlSubmit)="onSourceUrlSubmit($event)"
      ></app-feed-builder-source-aspect>
    }
    @case ('map') {
      <app-feed-builder-map-aspect
        [genericFeeds]="genericFeeds"
        [nativeFeeds]="nativeFeeds"
        [selectedFeed]="selectedFeed"
        [showCustomize]="sourceBuilder().hasFetchActionReturnedHtml()"
        [sourceBuilder]="sourceBuilder()"
        (feedSelected)="onFeedSelected($event)"
      ></app-feed-builder-map-aspect>
    }
    @case ('reduce') {
      <app-feed-builder-reduce-aspect
        [sourceBuilder]="sourceBuilder()"
        (filterChange)="onFilterChange($event)"
      ></app-feed-builder-reduce-aspect>
    }
  }
</div>
```

```typescript
import { buildAspects, previewModeForAspect } from '../feed-builder-flow/feed-builder-aspect.utils';
import { FeedBuilderAspectId } from '../feed-builder-flow/feed-builder-flow.types';

protected activeAspectId: FeedBuilderAspectId = 'source';

ngOnInit() {
  // after reloadFeedsAndRestoreSelection:
  this.activeAspectId = deriveInitialAspect(this.sourceBuilder(), this.isValid());
  // ...
}

setActiveAspect(aspectId: FeedBuilderAspectId) {
  if (aspectId === this.activeAspectId) return;
  this.activeAspectId = aspectId;
  this.syncPreviewForAspect(aspectId);
  this.changeRef.detectChanges();
  // Do NOT abort in-flight scrape — preview updates when request completes
}

previewMode() {
  return previewModeForAspect(this.activeAspectId, Boolean(this.selectedFeed));
}

private syncPreviewForAspect(aspectId: FeedBuilderAspectId) {
  if (aspectId === 'reduce') this.fetchFeedPreview(false);
  if (aspectId === 'map' && this.selectedFeed?.genericFeed) {
    this.sourceBuilder().events.showElements.next(
      this.selectedFeed.genericFeed.selectors.contextXPath
    );
  }
}
```

Preview always runs `scrapeService.scrape(sourceBuilder.build())` — the full `SourceInput` JSON including all plugins in `flow.sequence`. Aspect buttons change which preview panel is emphasized, not what gets scraped.

Update right column to respect `previewMode()`:

```html
@if (previewMode() === 'website' || previewMode() === 'both') {
  <app-interactive-website ...>
    @if (previewMode() === 'both') {
      <ion-segment-button segmentButton value="feed">Feed</ion-segment-button>
    }
    ...
  </app-interactive-website>
}
@if (previewMode() === 'feed') {
  <app-remote-feed-preview [items]="feedItems" [noMetaColumn]="true"></app-remote-feed-preview>
}
```

Remove `IonAccordion` / `IonAccordionGroup` from imports.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/app-web && npm test -- --testPathPattern=transform-website-to-feed.component.spec.ts -v`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/transform-website-to-feed/
git commit -m "feat(feed-builder): aspect-driven layout over SourceInput JSON"
```

---

### Task 6: Simplify feed-builder host (SourceBuilder only)

**Files:**
- Modify: `packages/app-web/src/app/components/feed-builder/feed-builder.component.html`
- Modify: `packages/app-web/src/app/components/feed-builder/feed-builder.component.ts`
- Modify: `packages/app-web/src/app/components/feed-builder/feed-builder.component.spec.ts`

Remove accordion projections. `FeedBuilderComponent` creates `SourceBuilder`, passes it to `app-transform-website-to-feed`. Aspect views live inside transform component — no `sourcePanel`/`reducePanel` slots needed.

```html
@if (sourceBuilder && sourceBuilder.response) {
  <app-transform-website-to-feed
    #webToFeedTransformer
    (selectedFeedChange)="selectedFeed = $event"
    (statusChange)="hasValidFeed = $event === 'valid'"
    [feed]="getFeed()"
    [sourceBuilder]="sourceBuilder"
  >
    <ion-list bottomSlot>
      <!-- Create Feed / Show Feed URL actions unchanged -->
    </ion-list>
  </app-transform-website-to-feed>
}
```

In `createOrRefineFeed()`, emit `sourceBuilder.build()` — the complete `GqlSourceInput` JSON.

Remove `onFilterChange` from `FeedBuilderComponent` (handled in reduce aspect). Standalone feed URL `q` param generation — deferred; keep `getFilterPlugin()` reading from `sourceBuilder` until addressed in follow-up.

- [ ] **Step 4: Run all feed-builder tests**

Run: `cd packages/app-web && npm test -- --testPathPattern=feed-builder -v`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/feed-builder/
git commit -m "feat(feed-builder): simplify host to SourceBuilder-driven aspects"
```

---

### Task 7: Styles, default step behavior, and manual verification

**Files:**
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.scss`
- Modify: `packages/app-web/src/app/components/feed-builder/feed-builder.component.scss`

- [ ] **Step 1: Add step content styles**

```scss
/* transform-website-to-feed.component.scss */
.feed-builder-aspect-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 150px;
}

@media (max-width: 768px) {
  :host app-responsive-columns {
    flex-direction: column;

    [left], [right] {
      flex: none;
      width: 100%;
    }
  }

  .feed-builder-aspect-content {
    max-height: 50vh;
    padding-bottom: 16px;
  }
}
```

- [ ] **Step 2: Verify default active step**

When `sourceBuilder.response` first appears, `activeAspectId` remains `'source'`. Button `configured` state derives from JSON via `buildAspects()` — not from separate UI flags.

Checklist:
1. Open feed builder, enter URL — `SourceInput.flow.sequence[0].fetch` updates
2. Three aspect buttons: **Source → Map → Reduce**
3. Map aspect: picking a feed writes `org_feedless_feed` to JSON; customize edits `SelectorsInput`
4. Reduce aspect: filters write `org_feedless_filter` array to JSON
5. `createOrRefineFeed()` emits `sourceBuilder.build()` matching GraphQL schema
6. Preview on each aspect reflects scrape of current `SourceInput`

- [ ] **Step 4: Run full related test suite**

Run: `cd packages/app-web && npm test -- --testPathPattern="feed-builder|transform-website-to-feed|filter-items" -v`

Expected: All PASS

- [ ] **Step 5: Commit**

```bash
git add packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.scss
git add packages/app-web/src/app/components/feed-builder/feed-builder.component.scss
git commit -m "style(feed-builder): polish flow step content layout"
```

---

### Task 8: Raw JSON alternative view (dev)

**Files:**
- Create: `packages/app-web/src/app/components/feed-builder-flow/feed-builder-json-aspect.component.*`
- Modify: `packages/app-web/src/app/components/transform-website-to-feed/transform-website-to-feed.component.html`

Add a dev-gated alternative to aspect views: a "JSON" segment tab in the preview toolbar showing `JSON.stringify(sourceBuilder.build(), null, 2)` in a code editor. On save, parse and call `sourceBuilder.overwriteFlow(parsed.flow.sequence)` + `patch(metadata)`. Complements Source/Map/Reduce aspects for power users.

Gate with `appDev` directive. Lives in preview toolbar, not in aspect nav.

- [ ] **Step 1:** Test JSON round-trip (parse → overwriteFlow → build matches)
- [ ] **Step 2–5:** Implement, test, commit

```bash
git commit -m "feat(feed-builder): add dev JSON editor alternative for SourceInput"
```

---

## Self-Review

**Spec coverage:**
| Requirement | Task |
|-------------|------|
| UI driven by `SourceInput` JSON (GraphQL schema) | Architecture, Task 1 (`feed-builder-aspect.utils.ts`), all aspect views |
| Aspect buttons select JSON slices | Task 1, 5 (`@switch` on `activeAspectId`) |
| Source aspect → metadata + fetch | Task 2 |
| Map aspect → `org_feedless_feed` / `SelectorsInput` | Task 3 |
| Reduce aspect → `org_feedless_filter` | Task 4 |
| Only one button active at a time | Task 1, 5 |
| Previews from `sourceBuilder.build()` | Task 5 |
| Scrape errors: red bubble on aspect + details in preview panel | Task 5, Task 7 |
| Native feed pick requires URL-change confirmation | Task 3 |
| Aspect nav `role="tablist"` a11y | Task 1 |
| No duplicate form state outside SourceBuilder | Task 2, 6 |
| Repository-modal filter unification | Deferred follow-up |
| Raw JSON editor as dev alternative | Task 8 |
| Standalone feed URL `q` param | Deferred follow-up |

**Placeholder scan:** No TBD/TODO placeholders in plan steps.

**Type consistency:** `FeedBuilderAspectId`, `buildAspects()`, and `@switch` cases all use `'source' | 'map' | 'reduce'`. Aspect views read/write via `SourceBuilder` methods that produce valid `GqlSourceInput`.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-07-07-feed-builder-flow-buttons.md`. Two execution options:

**1. Subagent-Driven (recommended)** — dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?

<!-- CHALLENGE-THE-PLAN-METADATA
{
  "round": 4,
  "status": "complete",
  "questionHistory": [
    {"q": "Selector writes trigger scrape?", "a": "mark-dirty only — no auto-scrape on field change", "category": "technical"},
    {"q": "Why not JSON editor instead of aspects?", "a": "add as dev alternative view (Task 8)", "category": "tradeOffs"},
    {"q": "Standalone URL filter read?", "a": "deferred", "category": "technical"},
    {"q": "Plan complete?", "a": "done", "category": "meta"}
  ],
  "deferredItems": [
    {"q": "Standalone feed URL q param generation after refactor", "category": "technical"},
    {"q": "Repository-modal filter accordion unification", "category": "technical"}
  ],
  "categoriesCovered": {
    "technical": {"stack": false, "architecture": true, "implementation": true},
    "domain": {"rules": true, "workflows": true},
    "ux": {"happyPath": true, "edgeCases": true, "errors": true, "accessibility": true},
    "nonFunctional": {"security": false, "performance": true, "scalability": false},
    "tradeOffs": true
  }
}
END-CHALLENGE-THE-PLAN-METADATA -->
