import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ProductsService } from '../service/products.service';
import { IProducts } from '../products.model';
import { ProductsFormService } from './products-form.service';

import { ProductsUpdateComponent } from './products-update.component';

describe('Products Management Update Component', () => {
  let comp: ProductsUpdateComponent;
  let fixture: ComponentFixture<ProductsUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let productsFormService: ProductsFormService;
  let productsService: ProductsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ProductsUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(ProductsUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ProductsUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    productsFormService = TestBed.inject(ProductsFormService);
    productsService = TestBed.inject(ProductsService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const products: IProducts = { id: 32203 };

      activatedRoute.data = of({ products });
      comp.ngOnInit();

      expect(comp.products).toEqual(products);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IProducts>>();
      const products = { id: 23443 };
      jest.spyOn(productsFormService, 'getProducts').mockReturnValue(products);
      jest.spyOn(productsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ products });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: products }));
      saveSubject.complete();

      // THEN
      expect(productsFormService.getProducts).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(productsService.update).toHaveBeenCalledWith(expect.objectContaining(products));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IProducts>>();
      const products = { id: 23443 };
      jest.spyOn(productsFormService, 'getProducts').mockReturnValue({ id: null });
      jest.spyOn(productsService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ products: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: products }));
      saveSubject.complete();

      // THEN
      expect(productsFormService.getProducts).toHaveBeenCalled();
      expect(productsService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IProducts>>();
      const products = { id: 23443 };
      jest.spyOn(productsService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ products });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(productsService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
