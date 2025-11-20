import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class RecaptchaService {
  private siteKey = (window as any).__RECAPTCHA_SITE_KEY__ || ''
  private loaded = false

  loadScript(){
    if(this.loaded || !this.siteKey) return Promise.resolve()
    return new Promise<void>((resolve, reject)=>{
      const existing = document.querySelector(`script[data-recaptcha="${this.siteKey}"]`)
      if(existing){ this.loaded = true; return resolve() }
      const s = document.createElement('script')
      s.src = `https://www.google.com/recaptcha/api.js?render=${this.siteKey}`
      s.async = true
      s.defer = true
      s.setAttribute('data-recaptcha', this.siteKey)
      s.onload = ()=>{ this.loaded = true; resolve() }
      s.onerror = (e)=> reject(e)
      document.head.appendChild(s)
    })
  }

  async execute(action: string){
    await this.loadScript()
    if(!(window as any).grecaptcha || !this.siteKey) return ''
    return (window as any).grecaptcha.execute(this.siteKey, { action })
  }
}
