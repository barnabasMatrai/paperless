import { Component, inject, signal } from '@angular/core';
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

  showPassword = signal(false);
  showRepeatPassword = signal(false);
  loading = signal(false);
  errorMessage = signal('');

  registerForm = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, this.noWhitespace]],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          this.noWhitespace,
        ],
      ],
      repeatPassword: ['', [Validators.required, this.noWhitespace]],
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

  private noWhitespace(
    control: AbstractControl
  ): ValidationErrors | null {
    const value = control.value as string;

    if (/\s/.test(value)) {
      return { whitespace: true };
    }

    return null;
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
    if (this.registerForm.invalid || this.loading()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { username, password } = this.registerForm.getRawValue();

    this.authService.register({ username, password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/login'], {
          state: { registeredUsername: username },
        });
      },
      error: (error) => {
        this.loading.set(false);

        if (error.status === 409) {
          this.errorMessage.set('This username is already taken.');
        } else if (error.status === 400) {
          this.errorMessage.set('Please check your input.');
        } else {
          this.errorMessage.set(
            'Registration failed. Please try again later.'
          );
        }
      },
    });
  }
}
