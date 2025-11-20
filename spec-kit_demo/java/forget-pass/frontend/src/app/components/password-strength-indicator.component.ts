import { Component, Input } from '@angular/core'
import { CommonModule } from '@angular/common'

@Component({
  selector: 'app-password-strength-indicator',
  standalone: true,
  imports: [CommonModule],
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

  get label(){
    switch(this.score){
      case 0: return 'Too weak'
      case 1: return 'Weak'
      case 2: return 'Medium'
      case 3: return 'Strong'
      default: return 'Unknown'
    }
  }
}
