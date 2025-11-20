import { passwordPolicyValidator } from './password.validator';
import { FormControl } from '@angular/forms';

describe('passwordPolicyValidator', () => {
  it('should return null for a strong valid password', () => {
    const control = new FormControl('Str0ng!Password123');
    const res = passwordPolicyValidator(control as any);
    expect(res).toBeNull();
  });

  it('should catch short passwords', () => {
    const control = new FormControl('Short1!');
    const res = passwordPolicyValidator(control as any);
    expect(res).toBeTruthy();
    expect(res!.minLength).toBeDefined();
  });

  it('should require uppercase, lowercase, number and special char', () => {
    const control = new FormControl('alllowercase1234');
    const res = passwordPolicyValidator(control as any);
    expect(res).toBeTruthy();
    expect(res!.upper).toBeTruthy();
    expect(res!.special).toBeTruthy();
  });

  it('should reject common passwords', () => {
    const control = new FormControl('password');
    const res = passwordPolicyValidator(control as any);
    expect(res).toBeTruthy();
    expect(res!.common).toBeTruthy();
  });
});
