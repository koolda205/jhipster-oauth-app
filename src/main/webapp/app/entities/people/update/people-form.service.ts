import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IPeople, NewPeople } from '../people.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPeople for edit and NewPeopleFormGroupInput for create.
 */
type PeopleFormGroupInput = IPeople | PartialWithRequiredKeyOf<NewPeople>;

type PeopleFormDefaults = Pick<NewPeople, 'id'>;

type PeopleFormGroupContent = {
  id: FormControl<IPeople['id'] | NewPeople['id']>;
  address: FormControl<IPeople['address']>;
  email: FormControl<IPeople['email']>;
  password: FormControl<IPeople['password']>;
  name: FormControl<IPeople['name']>;
};

export type PeopleFormGroup = FormGroup<PeopleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PeopleFormService {
  createPeopleFormGroup(people: PeopleFormGroupInput = { id: null }): PeopleFormGroup {
    const peopleRawValue = {
      ...this.getFormDefaults(),
      ...people,
    };
    return new FormGroup<PeopleFormGroupContent>({
      id: new FormControl(
        { value: peopleRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      address: new FormControl(peopleRawValue.address),
      email: new FormControl(peopleRawValue.email, {
        validators: [Validators.required],
      }),
      password: new FormControl(peopleRawValue.password, {
        validators: [Validators.required],
      }),
      name: new FormControl(peopleRawValue.name, {
        validators: [Validators.required],
      }),
    });
  }

  getPeople(form: PeopleFormGroup): IPeople | NewPeople {
    return form.getRawValue() as IPeople | NewPeople;
  }

  resetForm(form: PeopleFormGroup, people: PeopleFormGroupInput): void {
    const peopleRawValue = { ...this.getFormDefaults(), ...people };
    form.reset(
      {
        ...peopleRawValue,
        id: { value: peopleRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PeopleFormDefaults {
    return {
      id: null,
    };
  }
}
