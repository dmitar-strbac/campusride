import { createContext } from 'react';
import type { LoginRequest, RegisterRequest, User } from '../types/auth';

export type AuthContextValue = {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
