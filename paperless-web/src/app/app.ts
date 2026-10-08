import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RegisterComponent, LoginComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('paperless-web');
}
