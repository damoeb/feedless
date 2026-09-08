/**
 * jsdom exposes a constructable `CSSStyleSheet` but implements `adoptedStyleSheets`
 * on neither `Document` nor `ShadowRoot`. Stencil's runtime (pulled in by @ionic/core)
 * probes both when it is imported and adopts stylesheets on every component render,
 * so the gap surfaces as "Cannot convert undefined or null to object" at import time
 * and as unhandled rejections while rendering.
 */
const adoptedStyleSheets = new WeakMap<object, CSSStyleSheet[]>();

for (const proto of [
  typeof Document !== 'undefined' && Document.prototype,
  typeof ShadowRoot !== 'undefined' && ShadowRoot.prototype,
]) {
  if (proto && !('adoptedStyleSheets' in proto)) {
    Object.defineProperty(proto, 'adoptedStyleSheets', {
      configurable: true,
      get() {
        let sheets = adoptedStyleSheets.get(this);
        if (!sheets) {
          sheets = [];
          adoptedStyleSheets.set(this, sheets);
        }
        return sheets;
      },
      set(sheets: CSSStyleSheet[]) {
        adoptedStyleSheets.set(this, sheets);
      },
    });
  }
}

/**
 * Ionic's components pick their styles by mode (`ios`/`md`). Without this
 * initialization the mode is unset, Stencil registers `undefined` as a component's
 * style text and rendering fails. Imported dynamically so that it runs after the
 * polyfill above — Stencil reads the DOM as soon as it is loaded.
 */
const { initialize } = await import('@ionic/core/components');
initialize();

/**
 * jsdom implements no media queries at all. Both Ionic and the app's own colour-scheme
 * detection call `matchMedia` during construction, so a query that matches nothing is
 * enough to keep them on their default (light) branch.
 */
if (typeof window !== 'undefined' && !window.matchMedia) {
  window.matchMedia = (query: string) =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => undefined,
      removeListener: () => undefined,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
      dispatchEvent: () => false,
    }) as MediaQueryList;
}
