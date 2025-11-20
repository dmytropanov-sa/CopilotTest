import { Component, Input } from '@angular/core'

@Component({
  selector: 'app-password-strength-indicator',
  standalone: true,
  template: `
    <div aria-live="polite" class="mt-2 text-sm">
      <div *ngIf="score === 0" class="text-red-600">Too weak</div>
      <div *ngIf="score === 1" class="text-yellow-600">Weak</div>
      <div *ngIf="score === 2" class="text-amber-600">Medium</div>
      <div *ngIf="score === 3" class="text-green-600">Strong</div>
    </div>
  `
})
export class PasswordStrengthIndicatorComponent {
  @Input() score = 0
}
