import { Component, HostListener, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UpperCasePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, UpperCasePipe],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {
  auth = inject(AuthService);
  user = this.auth.user;
  isManager = this.auth.isManager;
  menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update(open => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  closeMenuOnEscape(): void {
    this.closeMenu();
  }

  logout(): void {
    this.closeMenu();
    this.auth.logout();
  }
}
