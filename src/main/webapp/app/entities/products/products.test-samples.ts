import { IProducts, NewProducts } from './products.model';

export const sampleWithRequiredData: IProducts = {
  id: 13437,
  title: 'colorless ew upside-down',
  price: 7170.76,
};

export const sampleWithPartialData: IProducts = {
  id: 29548,
  title: 'lock yippee ick',
  price: 11703.46,
};

export const sampleWithFullData: IProducts = {
  id: 19845,
  title: 'guzzle',
  category: 'lampoon',
  price: 25890.64,
};

export const sampleWithNewData: NewProducts = {
  title: 'per not',
  price: 14703.93,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
