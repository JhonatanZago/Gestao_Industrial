import { Routes } from '@angular/router';
import { authGuard, permissionGuard } from './core/guards';

export const routes: Routes = [
  { path: 'login', title: 'Login | Metalúrgica ERP', loadComponent: () => import('./pages/info-page.component').then(m => m.InfoPageComponent), data: { title: 'Login integrado' } },
  { path: 'forbidden', title: 'Acesso negado | Metalúrgica ERP', loadComponent: () => import('./pages/info-page.component').then(m => m.InfoPageComponent), data: { title: 'Acesso não autorizado' } },
  { path: 'app/dashboard', title: 'Visão Geral | Metalúrgica ERP', canActivate: [authGuard], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Visão Geral', screen: 'dashboard' } },
  { path: 'app/setores', title: 'Setores | Metalúrgica ERP', canActivate: [authGuard, permissionGuard('SETOR_VISUALIZAR')], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Setores', screen: 'sectors' } },
  { path: 'app/setores/:setorId', title: 'Dashboard do Setor | Metalúrgica ERP', canActivate: [authGuard], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Dashboard do Setor', screen: 'sector' } },
  { path: 'app/setores/:setorId/funcionarios/:codigo', title: 'Funcionário | Metalúrgica ERP', canActivate: [authGuard], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Funcionário', screen: 'employee' } },
  { path: 'app/setores/:setorId/pecas/:codigo', title: 'Peça | Metalúrgica ERP', canActivate: [authGuard], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Peça', screen: 'part' } },
  { path: 'app/importacoes', title: 'Importar Excel | Metalúrgica ERP', canActivate: [authGuard, permissionGuard('PRODUCAO_IMPORTAR')], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Importar Excel', screen: 'import' } },
  { path: 'app/importacoes-automaticas', title: 'Importações Automáticas | Metalúrgica ERP', canActivate: [authGuard, permissionGuard('OPERACAO_MONITORAR')], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Importações Automáticas', screen: 'automatic-imports' } },
  { path: 'app/relatorios', title: 'Relatórios | Metalúrgica ERP', canActivate: [authGuard, permissionGuard('RELATORIO_VISUALIZAR')], loadComponent: () => import('./pages/core-page.component').then(m => m.CorePageComponent), data: { title: 'Relatórios', screen: 'report' } },
  { path: '**', canActivate: [authGuard], loadComponent: () => import('./pages/info-page.component').then(m => m.InfoPageComponent), data: { title: 'Metalúrgica ERP' } }
];
