import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IOrders } from '../orders.model';
import { OrdersService } from '../service/orders.service';

const ordersResolve = (route: ActivatedRouteSnapshot): Observable<null | IOrders> => {
  const id = route.params.id;
  if (id) {
    return inject(OrdersService)
      .find(id)
      .pipe(
        mergeMap((orders: HttpResponse<IOrders>) => {
          if (orders.body) {
            return of(orders.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default ordersResolve;
