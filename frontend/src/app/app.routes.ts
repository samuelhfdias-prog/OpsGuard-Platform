import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/organizations', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () =>
      import('./shared/layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'organizations',
        loadComponent: () =>
          import('./features/organizations/org-list/org-list.component').then(m => m.OrgListComponent)
      },
      {
        path: 'collaborators',
        loadComponent: () =>
          import('./features/collaborators/collab-list/collab-list.component').then(m => m.CollabListComponent)
      },
      {
        path: 'devices',
        loadComponent: () =>
          import('./features/devices/device-list/device-list.component').then(m => m.DeviceListComponent)
      }
    ]
  },
  { path: '**', redirectTo: '/organizations' }
];
