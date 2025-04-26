import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ProductsDetailComponent } from './products-detail.component';

describe('Products Management Detail Component', () => {
  let comp: ProductsDetailComponent;
  let fixture: ComponentFixture<ProductsDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductsDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./products-detail.component').then(m => m.ProductsDetailComponent),
              resolve: { products: () => of({ id: 23443 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ProductsDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductsDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load products on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ProductsDetailComponent);

      // THEN
      expect(instance.products()).toEqual(expect.objectContaining({ id: 23443 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
