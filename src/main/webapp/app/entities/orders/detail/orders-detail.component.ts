import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IOrders } from '../orders.model';

@Component({
  selector: 'jhi-orders-detail',
  templateUrl: './orders-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class OrdersDetailComponent {
  orders = input<IOrders | null>(null);

  previousState(): void {
    window.history.back();
  }
}
