import { TestBed } from '@angular/core/testing';
import type { ComponentFixture } from '@angular/core/testing';
import { PasswordStrengthIndicatorComponent } from './password-strength-indicator.component';

describe('PasswordStrengthIndicatorComponent', () => {
  let component: PasswordStrengthIndicatorComponent;
  let fixture: any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordStrengthIndicatorComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(PasswordStrengthIndicatorComponent);
    component = fixture.componentInstance;
  });

  it('should display strength label for given score', () => {
    component.score = 3;
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Strong');
  });

  it('should map low scores to Weak', () => {
    component.score = 1;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Weak');
  });
});
