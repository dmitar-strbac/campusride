import { useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/authApi';
import type { LoginRequest, RegisterRequest, User } from '../types/auth';
import { AuthContext } from './authContextValue';

const TOKEN_KEY = 'campusride_token';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(Boolean(token));

  useEffect(() => {
    async function loadUser() {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        const currentUser = await authApi.getCurrentUser(token);
        setUser(currentUser);
      } catch {
        localStorage.removeItem(TOKEN_KEY);
        setToken(null);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    }

    loadUser();
  }, [token]);

  async function login(data: LoginRequest) {
    const response = await authApi.login(data);
    localStorage.setItem(TOKEN_KEY, response.token);
    setToken(response.token);

    const currentUser = await authApi.getCurrentUser(response.token);
    setUser(currentUser);
  }

  async function register(data: RegisterRequest) {
    const response = await authApi.register(data);
    localStorage.setItem(TOKEN_KEY, response.token);
    setToken(response.token);

    const currentUser = await authApi.getCurrentUser(response.token);
    setUser(currentUser);
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({
      user,
      token,
      isLoading,
      login,
      register,
      logout,
    }),
    [user, token, isLoading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
