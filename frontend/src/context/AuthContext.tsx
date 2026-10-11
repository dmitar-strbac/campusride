import axios from 'axios';
import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { authApi } from '../api/authApi';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';
import { AuthContext } from './authContextValue';

const TOKEN_KEY = 'campusride_token';

type Session = {
  token: string | null;
  user: User | null;
  isLoading: boolean;
  error: string | null;
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session>(() => {
    const token = localStorage.getItem(TOKEN_KEY);

    return {
      token,
      user: null,
      isLoading: Boolean(token),
      error: null,
    };
  });

  const tokenRef = useRef(session.token);
  const generation = useRef(0);
  const mounted = useRef(true);

  const logout = useCallback(() => {
    generation.current += 1;
    tokenRef.current = null;
    localStorage.removeItem(TOKEN_KEY);

    setSession({
      token: null,
      user: null,
      isLoading: false,
      error: null,
    });
  }, []);

  const retrySession = useCallback(async () => {
    const token = tokenRef.current;
    if (!token) return;

    const requestId = ++generation.current;

    setSession((current) => ({
      ...current,
      isLoading: true,
      error: null,
    }));

    try {
      const user = await authApi.getCurrentUser(token);

      if (!mounted.current || generation.current !== requestId) return;

      setSession({
        token,
        user,
        isLoading: false,
        error: null,
      });
    } catch (error: unknown) {
      if (!mounted.current || generation.current !== requestId) return;

      if (axios.isAxiosError(error) && error.response?.status === 401) {
        logout();
        return;
      }

      setSession((current) => ({
        ...current,
        isLoading: false,
        error: 'Your session could not be loaded. Please try again.',
      }));
    }
  }, [logout]);

  useEffect(() => {
    mounted.current = true;

    const timer = window.setTimeout(() => {
      void retrySession();
    }, 0);

    return () => {
      mounted.current = false;
      generation.current += 1;
      window.clearTimeout(timer);
    };
  }, [retrySession]);

  const authenticate = useCallback(async (request: () => Promise<AuthResponse>) => {
    const requestId = ++generation.current;

    try {
      const response = await request();

      if (!mounted.current || generation.current !== requestId) {
        throw new Error('Authentication was cancelled.');
      }

      const user = await authApi.getCurrentUser(response.token);

      if (!mounted.current || generation.current !== requestId) {
        throw new Error('Authentication was cancelled.');
      }

      localStorage.setItem(TOKEN_KEY, response.token);
      tokenRef.current = response.token;

      setSession({
        token: response.token,
        user,
        isLoading: false,
        error: null,
      });
    } catch (error: unknown) {
      if (mounted.current && generation.current === requestId) {
        setSession((current) => ({
          ...current,
          isLoading: false,
        }));
      }

      throw error;
    }
  }, []);

  const login = useCallback(
    (data: LoginRequest) => authenticate(() => authApi.login(data)),
    [authenticate],
  );

  const register = useCallback(
    (data: RegisterRequest) => authenticate(() => authApi.register(data)),
    [authenticate],
  );

  const value = useMemo(
    () => ({
      ...session,
      login,
      register,
      logout,
      retrySession,
    }),
    [session, login, register, logout, retrySession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
