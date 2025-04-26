import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { OrdersDetailComponent } from './orders-detail.component';

describe('Orders Management Detail Component', () => {
  let comp: OrdersDetailComponent;
  let fixture: ComponentFixture<OrdersDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./orders-detail.component').then(m => m.OrdersDetailComponent),
              resolve: { orders: () => of({ id: 21578 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(OrdersDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrdersDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load orders on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', OrdersDetailComponent);

      // THEN
      expect(instance.orders()).toEqual(expect.objectContaining({ id: 21578 }));
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
