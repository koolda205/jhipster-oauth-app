import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import OrdersResolve from './route/orders-routing-resolve.service';

const ordersRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/orders.component').then(m => m.OrdersComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/orders-detail.component').then(m => m.OrdersDetailComponent),
    resolve: {
      orders: OrdersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/orders-update.component').then(m => m.OrdersUpdateComponent),
    resolve: {
      orders: OrdersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/orders-update.component').then(m => m.OrdersUpdateComponent),
    resolve: {
      orders: OrdersResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default ordersRoute;
