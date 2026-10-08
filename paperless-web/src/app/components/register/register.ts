import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);

  showPassword = false;
  showRepeatPassword = false;
  loading = false;

  registerForm = this.fb.nonNullable.group(
    {
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      repeatPassword: ['', Validators.required],
    },
    {
      validators: this.passwordMatchValidator,
    }
  );

  get username() {
    return this.registerForm.controls.username;
  }

  get password() {
    return this.registerForm.controls.password;
  }

  get repeatPassword() {
    return this.registerForm.controls.repeatPassword;
  }

  get passwordsDoNotMatch(): boolean {
    return (
      this.password.valid &&
      this.repeatPassword.touched &&
      this.registerForm.hasError('passwordMismatch')
    );
  }

  private passwordMatchValidator(
    control: AbstractControl
  ): ValidationErrors | null {
    const password = control.get('password')?.value;
    const repeatPassword = control.get('repeatPassword')?.value;

    if (!password || !repeatPassword) {
      return null;
    }

    return password === repeatPassword
      ? null
      : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const { username, password } = this.registerForm.getRawValue();

    // TODO: AuthService aufrufen, z.B.
    // this.authService.register(username, password).subscribe({
    //   next: () => { ... },
    //   error: () => { this.loading = false; }
    // });

    console.log('Register:', { username, password });

    this.loading = false;
  }
}
