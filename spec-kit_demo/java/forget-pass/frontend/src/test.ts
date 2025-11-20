// Ensure the global Zone is defined before loading zone testing helpers
import 'zone.js';
// Required for Angular testing with zone.js v0.15+
import 'zone.js/testing';
/// <reference types="jasmine" />
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from '@angular/platform-browser-dynamic/testing';

declare const require: any;

getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting()
);

// Explicit imports for test files (avoid runtime require.context which may not be available
// in certain bundler/runtime configurations). Add new spec imports here as you create them.
import './app/validators/password.validator.spec';
import './app/components/password-strength-indicator.component.spec';

