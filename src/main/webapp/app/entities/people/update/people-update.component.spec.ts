import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { PeopleService } from '../service/people.service';
import { IPeople } from '../people.model';
import { PeopleFormService } from './people-form.service';

import { PeopleUpdateComponent } from './people-update.component';

describe('People Management Update Component', () => {
  let comp: PeopleUpdateComponent;
  let fixture: ComponentFixture<PeopleUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let peopleFormService: PeopleFormService;
  let peopleService: PeopleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PeopleUpdateComponent],
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
      .overrideTemplate(PeopleUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PeopleUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    peopleFormService = TestBed.inject(PeopleFormService);
    peopleService = TestBed.inject(PeopleService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const people: IPeople = { id: 20275 };

      activatedRoute.data = of({ people });
      comp.ngOnInit();

      expect(comp.people).toEqual(people);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPeople>>();
      const people = { id: 9353 };
      jest.spyOn(peopleFormService, 'getPeople').mockReturnValue(people);
      jest.spyOn(peopleService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ people });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: people }));
      saveSubject.complete();

      // THEN
      expect(peopleFormService.getPeople).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(peopleService.update).toHaveBeenCalledWith(expect.objectContaining(people));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPeople>>();
      const people = { id: 9353 };
      jest.spyOn(peopleFormService, 'getPeople').mockReturnValue({ id: null });
      jest.spyOn(peopleService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ people: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: people }));
      saveSubject.complete();

      // THEN
      expect(peopleFormService.getPeople).toHaveBeenCalled();
      expect(peopleService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPeople>>();
      const people = { id: 9353 };
      jest.spyOn(peopleService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ people });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(peopleService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
