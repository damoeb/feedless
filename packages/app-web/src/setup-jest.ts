import './zone-flags';
import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
);

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

global.ResizeObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(),
}));

global.IntersectionObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(),
}));

Object.defineProperty(window, 'CSS', {
  value: {
    supports: jest.fn().mockReturnValue(false),
  },
});

HTMLCanvasElement.prototype.getContext = jest.fn();

Object.defineProperty(global, 'URL', {
  value: class MockURL {
    constructor(url) {
      this.href = url;
      this.search = url.includes('?') ? url.substring(url.indexOf('?')) : '';
      this.pathname = url.includes('?')
        ? url.substring(0, url.indexOf('?'))
        : url;
    }

    static createObjectURL = jest.fn(() => 'mocked-object-url');
    static revokeObjectURL = jest.fn();
  },
  writable: true,
});

global.spyOn = jest.spyOn;

process.on('unhandledRejection', (reason, promise) => {
  console.warn('Unhandled Promise Rejection:', reason);
  // Don't throw to prevent worker crashes
});

process.on('uncaughtException', (error) => {
  console.warn('Uncaught Exception:', error);
  // Don't exit to prevent worker crashes
});

const FDBFactory = require('fake-indexeddb/lib/FDBFactory');
const FDBKeyRange = require('fake-indexeddb/lib/FDBKeyRange');

global.indexedDB = new FDBFactory();
global.IDBKeyRange = FDBKeyRange;

if (!global.structuredClone) {
  global.structuredClone = (obj) => {
    return JSON.parse(JSON.stringify(obj));
  };
}

if (!global.location) {
  Object.defineProperty(global, 'location', {
    value: {
      href: 'http://localhost:3000',
      origin: 'http://localhost:3000',
      protocol: 'http:',
      host: 'localhost:3000',
      hostname: 'localhost',
      port: '3000',
      pathname: '/',
      search: '',
      hash: '',
      assign: jest.fn(),
      replace: jest.fn(),
      reload: jest.fn(),
    },
    writable: true,
  });
}
