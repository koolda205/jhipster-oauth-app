export interface IOrders {
  id: number;
  userId?: number | null;
  productId?: number | null;
  total?: number | null;
  quantity?: number | null;
}

export type NewOrders = Omit<IOrders, 'id'> & { id: null };
