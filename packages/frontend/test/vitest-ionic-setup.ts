import { initialize } from '@ionic/core/components';

/**
 * Ionic's components pick their styles by mode (`ios`/`md`). Without this
 * initialization the mode is unset, Stencil registers `undefined` as a component's
 * style text and rendering fails. A separate setup file so that it loads after the
 * polyfills in vitest-setup.ts — Stencil reads the DOM as soon as it is loaded.
 */
initialize();
