import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { RegistrationComponent } from './components/registration.component';

@NgModule({
  declarations: [],
  imports: [BrowserModule, ReactiveFormsModule, RegistrationComponent],
  providers: [],
  bootstrap: [RegistrationComponent]
})
export class AppModule {}
