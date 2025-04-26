import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IProducts } from '../products.model';
import { ProductsService } from '../service/products.service';

@Component({
  templateUrl: './products-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ProductsDeleteDialogComponent {
  products?: IProducts;

  protected productsService = inject(ProductsService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.productsService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
