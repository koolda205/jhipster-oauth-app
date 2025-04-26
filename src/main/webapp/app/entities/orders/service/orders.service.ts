import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IOrders, NewOrders } from '../orders.model';

export type PartialUpdateOrders = Partial<IOrders> & Pick<IOrders, 'id'>;

export type EntityResponseType = HttpResponse<IOrders>;
export type EntityArrayResponseType = HttpResponse<IOrders[]>;

@Injectable({ providedIn: 'root' })
export class OrdersService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/orders');

  create(orders: NewOrders): Observable<EntityResponseType> {
    return this.http.post<IOrders>(this.resourceUrl, orders, { observe: 'response' });
  }

  update(orders: IOrders): Observable<EntityResponseType> {
    return this.http.put<IOrders>(`${this.resourceUrl}/${this.getOrdersIdentifier(orders)}`, orders, { observe: 'response' });
  }

  partialUpdate(orders: PartialUpdateOrders): Observable<EntityResponseType> {
    return this.http.patch<IOrders>(`${this.resourceUrl}/${this.getOrdersIdentifier(orders)}`, orders, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IOrders>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IOrders[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getOrdersIdentifier(orders: Pick<IOrders, 'id'>): number {
    return orders.id;
  }

  compareOrders(o1: Pick<IOrders, 'id'> | null, o2: Pick<IOrders, 'id'> | null): boolean {
    return o1 && o2 ? this.getOrdersIdentifier(o1) === this.getOrdersIdentifier(o2) : o1 === o2;
  }

  addOrdersToCollectionIfMissing<Type extends Pick<IOrders, 'id'>>(
    ordersCollection: Type[],
    ...ordersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const orders: Type[] = ordersToCheck.filter(isPresent);
    if (orders.length > 0) {
      const ordersCollectionIdentifiers = ordersCollection.map(ordersItem => this.getOrdersIdentifier(ordersItem));
      const ordersToAdd = orders.filter(ordersItem => {
        const ordersIdentifier = this.getOrdersIdentifier(ordersItem);
        if (ordersCollectionIdentifiers.includes(ordersIdentifier)) {
          return false;
        }
        ordersCollectionIdentifiers.push(ordersIdentifier);
        return true;
      });
      return [...ordersToAdd, ...ordersCollection];
    }
    return ordersCollection;
  }
}
