import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => inject(AuthService).hasSession() || inject(Router).parseUrl('/login');
export const permissionGuard = (permission: string): CanActivateFn => () => inject(AuthService).hasPermission(permission) || inject(Router).parseUrl('/forbidden');
