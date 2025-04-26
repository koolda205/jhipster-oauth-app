import { IPeople, NewPeople } from './people.model';

export const sampleWithRequiredData: IPeople = {
  id: 18053,
  email: 'Aleksandr_Savina64@mail.ru',
  password: 'outstanding',
  name: 'gadzooks how hard-to-find',
};

export const sampleWithPartialData: IPeople = {
  id: 20743,
  email: 'Nonna_Fedorov@yahoo.com',
  password: 'below mysterious',
  name: 'even enlightened',
};

export const sampleWithFullData: IPeople = {
  id: 20469,
  address: 'inside',
  email: 'Mokei5@gmail.com',
  password: 'if',
  name: 'because',
};

export const sampleWithNewData: NewPeople = {
  email: 'Leonid72@ya.ru',
  password: 'athwart without famously',
  name: 'wrongly cleverly cuddly',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
