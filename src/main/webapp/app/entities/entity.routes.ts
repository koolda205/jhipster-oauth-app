import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'jhipsterOauthApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'people',
    data: { pageTitle: 'jhipsterOauthApp.people.home.title' },
    loadChildren: () => import('./people/people.routes'),
  },
  {
    path: 'orders',
    data: { pageTitle: 'jhipsterOauthApp.orders.home.title' },
    loadChildren: () => import('./orders/orders.routes'),
  },
  {
    path: 'products',
    data: { pageTitle: 'jhipsterOauthApp.products.home.title' },
    loadChildren: () => import('./products/products.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
