import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IOrders } from '../orders.model';
import { OrdersService } from '../service/orders.service';

@Component({
  templateUrl: './orders-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class OrdersDeleteDialogComponent {
  orders?: IOrders;

  protected ordersService = inject(OrdersService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.ordersService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
