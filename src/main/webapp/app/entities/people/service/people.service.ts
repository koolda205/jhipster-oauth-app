import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPeople, NewPeople } from '../people.model';

export type PartialUpdatePeople = Partial<IPeople> & Pick<IPeople, 'id'>;

export type EntityResponseType = HttpResponse<IPeople>;
export type EntityArrayResponseType = HttpResponse<IPeople[]>;

@Injectable({ providedIn: 'root' })
export class PeopleService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/people');

  create(people: NewPeople): Observable<EntityResponseType> {
    return this.http.post<IPeople>(this.resourceUrl, people, { observe: 'response' });
  }

  update(people: IPeople): Observable<EntityResponseType> {
    return this.http.put<IPeople>(`${this.resourceUrl}/${this.getPeopleIdentifier(people)}`, people, { observe: 'response' });
  }

  partialUpdate(people: PartialUpdatePeople): Observable<EntityResponseType> {
    return this.http.patch<IPeople>(`${this.resourceUrl}/${this.getPeopleIdentifier(people)}`, people, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPeople>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPeople[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPeopleIdentifier(people: Pick<IPeople, 'id'>): number {
    return people.id;
  }

  comparePeople(o1: Pick<IPeople, 'id'> | null, o2: Pick<IPeople, 'id'> | null): boolean {
    return o1 && o2 ? this.getPeopleIdentifier(o1) === this.getPeopleIdentifier(o2) : o1 === o2;
  }

  addPeopleToCollectionIfMissing<Type extends Pick<IPeople, 'id'>>(
    peopleCollection: Type[],
    ...peopleToCheck: (Type | null | undefined)[]
  ): Type[] {
    const people: Type[] = peopleToCheck.filter(isPresent);
    if (people.length > 0) {
      const peopleCollectionIdentifiers = peopleCollection.map(peopleItem => this.getPeopleIdentifier(peopleItem));
      const peopleToAdd = people.filter(peopleItem => {
        const peopleIdentifier = this.getPeopleIdentifier(peopleItem);
        if (peopleCollectionIdentifiers.includes(peopleIdentifier)) {
          return false;
        }
        peopleCollectionIdentifiers.push(peopleIdentifier);
        return true;
      });
      return [...peopleToAdd, ...peopleCollection];
    }
    return peopleCollection;
  }
}
