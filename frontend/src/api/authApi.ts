import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';
import { apiClient } from './apiClient';

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  getCurrentUser: async (token: string): Promise<User> => {
    const response = await apiClient.get<User>('/users/me', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return response.data;
  },
};
