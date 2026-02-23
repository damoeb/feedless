import { setupZoneTestEnv } from 'jest-preset-angular/setup-env/zone';

// matchMedia stub for components that use color-scheme / media queries
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: () => {
    return {};
  },
});

// Stub Ionic/Stencil custom elements so they don't run __registerHost in jsdom
const ionTags = [
  'ion-app',
  'ion-content',
  'ion-header',
  'ion-footer',
  'ion-toolbar',
  'ion-title',
  'ion-buttons',
  'ion-button',
  'ion-searchbar',
  'ion-select',
  'ion-select-option',
  'ion-list',
  'ion-item',
  'ion-spinner',
  'ion-text',
  'ion-note',
  'ion-input',
  'ion-radio-group',
  'ion-radio',
  'ion-label',
  'ion-checkbox',
  'ion-badge',
  'ion-menu-button',
  'ion-icon',
];

class IonStub extends HTMLElement {}

ionTags.forEach((tag) => {
  if (!customElements.get(tag)) {
    customElements.define(tag, class extends IonStub {});
  }
});

setupZoneTestEnv({
  errorOnUnknownElements: true,
  errorOnUnknownProperties: true,
});
