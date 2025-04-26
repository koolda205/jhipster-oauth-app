import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IProducts } from '../products.model';

@Component({
  selector: 'jhi-products-detail',
  templateUrl: './products-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class ProductsDetailComponent {
  products = input<IProducts | null>(null);

  previousState(): void {
    window.history.back();
  }
}
