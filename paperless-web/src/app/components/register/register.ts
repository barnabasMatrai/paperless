import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  showPassword = false;
  showRepeatPassword = false;
  loading = false;
  errorMessage = '';

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
    if (this.registerForm.invalid || this.loading) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { username, password } = this.registerForm.getRawValue();

    this.authService
      .register({ username, password })
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/login'], {
            state: { registeredUsername: username },
          });
        },
        error: (error) => {
          this.loading = false;

          if (error.status === 409) {
            this.errorMessage = 'This username is already taken.';
          } else if (error.status === 400) {
            this.errorMessage = 'Please check your input.';
          } else {
            this.errorMessage =
              'Registration failed. Please try again later.';
          }
        },
      });
  }
}
