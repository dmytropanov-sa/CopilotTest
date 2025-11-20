import { Component } from '@angular/core'
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'
import { CommonModule } from '@angular/common'
import { RecaptchaService } from '../services/recaptcha.service'
import { PasswordStrengthIndicatorComponent } from './password-strength-indicator.component'

function passwordValidator(control: any){
  const value = control.value || ''
  const errors: any = {}
  if(value.length < 12) errors.minLength = true
  if(!/[A-Z]/.test(value)) errors.upper = true
  if(!/[a-z]/.test(value)) errors.lower = true
  if(!/\d/.test(value)) errors.number = true
  if(!/[!@#\$%\^&\*]/.test(value)) errors.special = true
  return Object.keys(errors).length ? errors : null
}

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PasswordStrengthIndicatorComponent],
  template: `
  <main class="max-w-md mx-auto bg-white p-6 rounded shadow mt-6">
    <h2 class="text-xl font-semibold mb-4">Create account</h2>
    <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
      <label class="block">
        <span class="text-sm font-medium">Email</span>
        <input formControlName="email" type="email" class="mt-1 block w-full border rounded px-3 py-2" aria-describedby="emailHelp" />
        <div id="emailHelp" class="text-xs text-red-600" *ngIf="form.controls.email.invalid && form.controls.email.touched">Invalid email</div>
      </label>

      <label class="block">
        <span class="text-sm font-medium">Date of birth</span>
        <input formControlName="dob" type="date" class="mt-1 block w-full border rounded px-3 py-2" />
        <div class="text-xs text-red-600" *ngIf="form.controls.dob.invalid && form.controls.dob.touched">You must be 18+</div>
      </label>

      <label class="block">
        <span class="text-sm font-medium">Password</span>
        <input formControlName="password" type="password" class="mt-1 block w-full border rounded px-3 py-2" (input)="onPassword()" aria-describedby="pwHelp" />
        <div id="pwHelp" class="text-xs text-red-600" *ngIf="form.controls.password.invalid && form.controls.password.touched">Password does not meet requirements</div>
        <app-password-strength-indicator [score]="pwScore"></app-password-strength-indicator>
      </label>

      <div>
        <button class="btn-primary w-full" [disabled]="submitting || form.invalid">Register</button>
      </div>
    </form>
    <div *ngIf="message" class="mt-3 text-sm">{{ message }}</div>
  </main>
  `
})
export class RegistrationComponent {
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    dob: ['', [Validators.required, this.dobValidator]],
    password: ['', [Validators.required, passwordValidator]]
  })
  pwScore = 0
  message = ''
  submitting = false

  constructor(private fb: FormBuilder, private recaptcha: RecaptchaService){ }

  dobValidator(control: any){
    const v = control.value
    if(!v) return { required: true }
    const dob = new Date(v)
    const now = new Date()
    const age = now.getFullYear() - dob.getFullYear() - (now.getMonth() < dob.getMonth() || (now.getMonth() === dob.getMonth() && now.getDate() < dob.getDate()) ? 1 : 0)
    return age >= 18 ? null : { underage: true }
  }

  onPassword(){
    const v = this.form.controls.password.value || ''
    let score = 0
    if(v.length >= 12) score++
    if(/[A-Z]/.test(v)) score++
    if(/[a-z]/.test(v)) score++
    if(/\d/.test(v)) score++
    if(/[!@#\$%\^&\*]/.test(v)) score++
    this.pwScore = Math.min(3, Math.floor(score / 2))
  }

  async submit(){
    if(this.form.invalid) return
    this.submitting = true
    this.message = ''
    try{
      const token = await this.recaptcha.execute('register')
      const payload = { email: this.form.value.email, password: this.form.value.password, dob: this.form.value.dob }
      const res = await fetch('/api/register', {
        method: 'POST', headers: { 'Content-Type': 'application/json', 'X-ReCaptcha-Token': token || '' }, body: JSON.stringify(payload)
      })
      if(res.ok) this.message = 'Registration submitted — check your email for verification'
      else this.message = 'Registration failed: ' + (await res.text())
    } catch(err){
      this.message = 'Network error: ' + String(err)
    } finally { this.submitting = false }
  }
}
