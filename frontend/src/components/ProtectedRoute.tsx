import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, isLoading, error, retrySession, logout } = useAuth();

  if (isLoading || (!user && error)) {
    return (
      <main className="flex min-h-[calc(100svh-65px)] items-center justify-center bg-[#060e1a] px-6">
        <div className="max-w-sm text-center">
          <p role={error ? 'alert' : 'status'} className="text-sm text-slate-300">
            {isLoading ? 'Loading your session...' : error}
          </p>

          {!isLoading && error && (
            <div className="mt-5 flex justify-center gap-3">
              <button
                type="button"
                onClick={() => void retrySession()}
                className="app-button-primary"
              >
                Try again
              </button>

              <button type="button" onClick={logout} className="app-button-secondary">
                Sign out
              </button>
            </div>
          )}
        </div>
      </main>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
