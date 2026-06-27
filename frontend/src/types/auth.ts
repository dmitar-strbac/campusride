export type User = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'STUDENT' | 'ADMIN';
};

export type AuthResponse = {
  token: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
};
