import { IOrders, NewOrders } from './orders.model';

export const sampleWithRequiredData: IOrders = {
  id: 13918,
  userId: 14800,
  productId: 12268,
  total: 14675.21,
  quantity: 22114,
};

export const sampleWithPartialData: IOrders = {
  id: 28299,
  userId: 23946,
  productId: 25947,
  total: 13600.64,
  quantity: 13683,
};

export const sampleWithFullData: IOrders = {
  id: 32114,
  userId: 25691,
  productId: 19024,
  total: 15472.6,
  quantity: 12264,
};

export const sampleWithNewData: NewOrders = {
  userId: 30112,
  productId: 21277,
  total: 19605.55,
  quantity: 4506,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
