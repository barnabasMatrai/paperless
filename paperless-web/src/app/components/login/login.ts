import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
  private fb = inject(FormBuilder);

  showPassword = false;
  loading = false;

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    rememberMe: [false],
  });

  get email() { return this.loginForm.controls.email; }
  get password() { return this.loginForm.controls.password; }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    const { email, password, rememberMe } = this.loginForm.getRawValue();

    // TODO: AuthService aufrufen, z.B.
    // this.authService.login(email, password, rememberMe).subscribe({...})
    console.log('Login:', { email, rememberMe });
    this.loading = false;
  }
}
