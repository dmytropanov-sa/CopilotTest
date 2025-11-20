import { AbstractControl, ValidationErrors } from '@angular/forms'
import { COMMON_PASSWORDS } from '../data/common-passwords'

export function passwordPolicyValidator(control: AbstractControl): ValidationErrors | null {
  const v = control.value || ''
  const errors: any = {}
  if(v.length < 12) errors.minLength = { requiredLength: 12, actualLength: v.length }
  if(!/[A-Z]/.test(v)) errors.upper = true
  if(!/[a-z]/.test(v)) errors.lower = true
  if(!/\d/.test(v)) errors.number = true
  if(!/[!@#\$%\^&\*\(\)\-_\+=\[\]{};:'"\\|,.<>\/?`~]/.test(v)) errors.special = true
  const lower = v.toLowerCase()
  if(COMMON_PASSWORDS.includes(lower)) errors.common = true
  return Object.keys(errors).length ? errors : null
}
