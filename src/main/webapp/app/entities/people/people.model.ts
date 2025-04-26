export interface IPeople {
  id: number;
  address?: string | null;
  email?: string | null;
  password?: string | null;
  name?: string | null;
}

export type NewPeople = Omit<IPeople, 'id'> & { id: null };
