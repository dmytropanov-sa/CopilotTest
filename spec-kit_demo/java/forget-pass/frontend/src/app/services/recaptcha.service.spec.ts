import { TestBed } from '@angular/core/testing';
import { RecaptchaService } from './recaptcha.service';

describe('RecaptchaService', () => {
  let service: RecaptchaService;

  beforeEach(() => {
    (window as any).__RECAPTCHA_SITE_KEY__ = 'test-key';
    (window as any).grecaptcha = {
      ready: (fn: any) => fn(),
      execute: async (_k: string, _opts: any) => 'token-123'
    };
    TestBed.configureTestingModule({ providers: [RecaptchaService] });
    service = TestBed.inject(RecaptchaService);
  });

  it('execute returns token when grecaptcha set', async () => {
    (window as any).__RECAPTCHA_SITE_KEY__ = 'test-key';
    (window as any).grecaptcha = {
      ready: (fn: any) => fn(),
      execute: async (_k: string, _opts: any) => 'token-123'
    };
    const t = await service.execute('register');
    expect(t).toBe('token-123');
  });
});
