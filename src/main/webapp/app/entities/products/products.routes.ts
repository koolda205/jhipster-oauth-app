import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ProductsResolve from './route/products-routing-resolve.service';

const productsRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/products.component').then(m => m.ProductsComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/products-detail.component').then(m => m.ProductsDetailComponent),
    resolve: {
      products: ProductsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/products-update.component').then(m => m.ProductsUpdateComponent),
    resolve: {
      products: ProductsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/products-update.component').then(m => m.ProductsUpdateComponent),
    resolve: {
      products: ProductsResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default productsRoute;
