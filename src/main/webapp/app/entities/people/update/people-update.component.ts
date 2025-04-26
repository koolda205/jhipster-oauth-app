import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPeople } from '../people.model';
import { PeopleService } from '../service/people.service';
import { PeopleFormGroup, PeopleFormService } from './people-form.service';

@Component({
  selector: 'jhi-people-update',
  templateUrl: './people-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PeopleUpdateComponent implements OnInit {
  isSaving = false;
  people: IPeople | null = null;

  protected peopleService = inject(PeopleService);
  protected peopleFormService = inject(PeopleFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PeopleFormGroup = this.peopleFormService.createPeopleFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ people }) => {
      this.people = people;
      if (people) {
        this.updateForm(people);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const people = this.peopleFormService.getPeople(this.editForm);
    if (people.id !== null) {
      this.subscribeToSaveResponse(this.peopleService.update(people));
    } else {
      this.subscribeToSaveResponse(this.peopleService.create(people));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPeople>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(people: IPeople): void {
    this.people = people;
    this.peopleFormService.resetForm(this.editForm, people);
  }
}
