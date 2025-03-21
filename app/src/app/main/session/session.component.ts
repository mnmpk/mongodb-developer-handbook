import { Component } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { SessionService } from './session.service';

@Component({
  selector: 'app-session',
  templateUrl: './session.component.html',
  styleUrl: './session.component.scss',
  standalone: false
})
export class SessionComponent {
  form!: FormGroup;
  loading = false;
  sessionData: any
  constructor(private formBuilder: FormBuilder, private service: SessionService) {

  }
  ngOnInit(): void {
    this.form = this.formBuilder.group({
      uId: [null],
      text: [null],
    });
  }
  put() {
    if (this.form.valid) {
      this.loading = true;
      this.service.put(this.form.value.text).subscribe({
        next: (result: any) => {
          this.sessionData = result;
          this.loading = false;
        },
        error: (err: any) => {
          alert(err.message);
          this.loading = false;
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
  clear() {
    this.service.clear().subscribe({
      next: (result: any) => {
        this.sessionData = result;
        this.loading = false;
      },
      error: (err: any) => {
        alert(err.message);
        this.loading = false;
      }
    });
  }
  login() {
    if (this.form.valid) {
      this.service.login(this.form.value.uId).subscribe({
        next: (result: any) => {
          this.sessionData = result;
          this.loading = false;
        },
        error: (err: any) => {
          alert(err.message);
          this.loading = false;
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
  logout() {
    this.service.logout().subscribe({
      next: (result: any) => {
        this.sessionData = result;
        this.loading = false;
      },
      error: (err: any) => {
        alert(err.message);
        this.loading = false;
      }
    });
  }
}
