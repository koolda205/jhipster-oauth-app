export interface IProducts {
  id: number;
  title?: string | null;
  category?: string | null;
  price?: number | null;
}

export type NewProducts = Omit<IProducts, 'id'> & { id: null };
