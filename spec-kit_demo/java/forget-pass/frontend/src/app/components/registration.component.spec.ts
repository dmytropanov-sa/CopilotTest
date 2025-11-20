import { TestBed } from '@angular/core/testing';
import { RegistrationComponent } from './registration.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PasswordStrengthIndicatorComponent } from './password-strength-indicator.component';
import { RecaptchaService } from '../services/recaptcha.service';

class MockRecaptcha {
  token = 'mock-token'
  async execute(_action: string){ return this.token }
}

describe('RegistrationComponent', () => {
  let fixture: any;
  let component: RegistrationComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, ReactiveFormsModule, PasswordStrengthIndicatorComponent],
      providers: [{ provide: RecaptchaService, useClass: MockRecaptcha }]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationComponent);
    component = fixture.componentInstance;
  });

  it('form invalid when empty', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('dob validator rejects underage', () => {
    const ctrl = component.form.controls.dob;
    ctrl.setValue('2010-01-01');
    expect(ctrl.errors).toBeTruthy();
    expect(ctrl.errors!.underage).toBeTruthy();
  });

  it('submit posts when valid', async () => {
    // recaptcha is injected via provider
    const spyFetch = spyOn(window as any, 'fetch').and.returnValue(Promise.resolve({ ok: true, status: 200, text: async () => '' } as any));

    component.form.controls.email.setValue('test@example.com');
    component.form.controls.dob.setValue('1990-01-01');
    component.form.controls.password.setValue('Str0ng!Password12');

    await component.submit();
    expect(spyFetch).toHaveBeenCalled();
    expect(component.message).toContain('Registration submitted');
  }, 5000);
});
